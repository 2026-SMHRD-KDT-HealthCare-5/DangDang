package com.dangdang.service;

import com.dangdang.dto.response.FoodRecognitionResponse;
import com.dangdang.dto.response.PreGlucoseResponse;
import com.dangdang.entity.DiagnosisGroup;
import com.dangdang.entity.User;
import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import com.dangdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * [각주 T] 음식 인식/AI 재분석 요청을 FastAPI(AI 서버)로 그대로 전달(프록시)하는 서비스입니다.
 * Spring은 여기서 DB에 아무것도 쓰지 않습니다 — FastAPI 응답을 안드로이드에 그대로 돌려주기만 합니다.
 * (기획서 아키텍처 원칙: DB 쓰기는 전부 Spring, AI 추론은 전부 FastAPI가 전담하는데,
 *  "먹은 음식 최종 확정" 시점에야 DB에 저장하고, 인식/재분석 단계는 아직 저장할 대상이 없습니다.)
 */

/*
* @Slf4j는 Lombok이 제공하는 로깅[1]용 어노테이션
* ex) private static final Logger log = LoggerFactory.getLogger(IntakeLogService.class);
* 을 자동으로 생성해줌. 그래서 따로 길게 코드를 안쓰고 편하게 log를 쓸 수 있음
* */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntakeLogService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    @Value("${fastapi.internal-api-key}")
    private String internalApiKey;

    // [각주 Y] ADA(미국당뇨병학회)/대한당뇨병학회 기준 HbA1c 구간 경계값(%).
    // 5.7 미만=건강군, 5.7~6.5 미만=전당뇨, 6.5 이상=2형당뇨.
    // ※ 노션 "식전 혈당 입력" 명세에 2형당뇨 기본값이 "140 vs 130 미확정"이라고 표시돼 있습니다.
    //   일단 FastAPI core/config.py의 PRE_GLUCOSE_DEFAULTS와 동일한 140으로 맞춰뒀는데,
    //   팀 합의가 나면 이 값(그리고 FastAPI 쪽 값)을 같이 바꿔야 합니다.
    private static final BigDecimal HBA1C_PREDIABETES_THRESHOLD = new BigDecimal("5.7");
    private static final BigDecimal HBA1C_DIABETES_THRESHOLD = new BigDecimal("6.5");
    private static final int PRE_GLUCOSE_DEFAULT_HEALTHY = 95;
    private static final int PRE_GLUCOSE_DEFAULT_PREDIABETES = 115;
    private static final int PRE_GLUCOSE_DEFAULT_DIABETES = 140;

    /**
     * [각주 X] POST /api/intake-logs/preglucose 가 호출합니다.
     * 사용자가 식전 혈당을 입력했으면 그 값을 그대로 돌려주고(preGlucoseDefault=null),
     * 입력하지 않았으면(null) 사용자의 hba1c 값으로 구간을 판정해 기본값을 계산해줍니다.
     *
     * ※ 여기서 쓰는 hba1c 구간 판정은 users.diagnosis_group 컬럼과는 별개입니다.
     *   diagnosis_group은 FastAPI 예측 모델에 전달하는 값(회원가입 때 선택 입력)이고,
     *   여기는 hba1c 수치 자체로 그때그때 계산합니다 — 회원가입 때 진단군을 입력 안 한
     *   사용자도 hba1c만 있으면 기본값을 받을 수 있게 하기 위함입니다.
     */
    public PreGlucoseResponse resolvePreGlucose(Integer userNo, Integer preGlucose) {
        if (preGlucose != null) {
            return new PreGlucoseResponse(preGlucose, null);
        }

        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        int defaultValue = resolveDefaultFromHba1c(user.getHba1c());
        return new PreGlucoseResponse(defaultValue, defaultValue);
    }

    private int resolveDefaultFromHba1c(BigDecimal hba1c) {
        // hba1c도 등록 안 한 사용자라면(선택 입력) 건강군 기본값으로 안전하게 처리합니다.
        if (hba1c == null) {
            return PRE_GLUCOSE_DEFAULT_HEALTHY;
        }
        if (hba1c.compareTo(HBA1C_DIABETES_THRESHOLD) >= 0) {
            return PRE_GLUCOSE_DEFAULT_DIABETES;
        }
        if (hba1c.compareTo(HBA1C_PREDIABETES_THRESHOLD) >= 0) {
            return PRE_GLUCOSE_DEFAULT_PREDIABETES;
        }
        return PRE_GLUCOSE_DEFAULT_HEALTHY;
    }

    /**
     * FastAPI의 POST /rag/intake-logs/recognize 를 호출합니다.
     * image/message 중 최소 하나는 있어야 하는데, 그 검증은 컨트롤러에서 먼저 합니다.
     *
     * [각주 V] diagnosisGroup: 요청에 값이 오면 그걸 우선 쓰고(안드로이드가 직접 지정하고 싶을 때 대비),
     * 안 오면 로그인한 사용자(userNo)의 users.diagnosis_group 값을 DB에서 조회해서 채웁니다.
     * 둘 다 없으면(진단군 미설정 회원) null로 FastAPI에 전달되고, FastAPI가 기본값("건강군")을 적용합니다.
     *
     * 요청으로 온 diagnosisGroup은 안드로이드 화면 문구("정상"/"전당뇨"/"제2형당뇨") 그대로이므로,
     * DiagnosisGroup.fromRawText()로 검증 + FastAPI가 이해하는 값("건강군"/"전당뇨"/"2형당뇨")으로
     * 변환합니다. DB에서 꺼낸 값(users.diagnosis_group)은 이미 변환된 값이라 그대로 씁니다.
     */
    public FoodRecognitionResponse recognizeFood(Integer userNo, MultipartFile image, String message,
                                                  Double baseline, String diagnosisGroup) {
        String resolvedDiagnosisGroup = (diagnosisGroup != null && !diagnosisGroup.isBlank())
                ? DiagnosisGroup.fromRawText(diagnosisGroup).getApiValue()
                : userRepository.findById(userNo).map(User::getDiagnosisGroup).orElse(null);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        if (image != null && !image.isEmpty()) {
            body.add("image", toResource(image));
        }
        if (message != null && !message.isBlank()) {
            body.add("message", message);
        }
        if (baseline != null) {
            body.add("baseline", baseline);
        }
        if (resolvedDiagnosisGroup != null && !resolvedDiagnosisGroup.isBlank()) {
            body.add("diagnosis_group", resolvedDiagnosisGroup);
        }

        HttpHeaders headers = new HttpHeaders();
        // [각주] FastAPI 라우터가 File(...)/Form(...)으로 값을 받기 때문에,
        // JSON이 아니라 반드시 multipart/form-data로 보내야 합니다.
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // [각주 W] FastAPI의 core/security.py(verify_internal_api_key)가 이 헤더를 검사합니다.
        // 없거나 값이 틀리면 FastAPI가 401로 거부합니다.
        headers.set("X-Internal-Api-Key", internalApiKey);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(
                    fastApiBaseUrl + "/rag/intake-logs/recognize",
                    requestEntity,
                    FoodRecognitionResponse.class
            );
        } catch (RestClientException e) {
            // AI 서버가 꺼져있거나 응답이 이상할 때. 서버가 죽지 않고 502로 안드로이드에 알려줍니다.
            log.error("FastAPI 호출 실패 (recognize): {}", e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }
    }

    /**
     * MultipartFile(스프링이 받은 업로드 파일)을 RestTemplate이 multipart 바디에 실을 수 있는
     * 형태로 바꿔줍니다. ByteArrayResource만 쓰면 파일 이름이 안 넘어가서, getFilename()을
     * 오버라이드해 원본 파일명을 보존합니다 (FastAPI가 image.content_type을 읽는 데 필요).
     */
    private ByteArrayResource toResource(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }
    }
}
