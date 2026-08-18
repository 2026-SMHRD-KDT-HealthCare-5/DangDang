package com.dangdang.service;

import com.dangdang.client.FastApiClient;
import com.dangdang.dto.response.ReanalyzeResponse;
import com.dangdang.entity.DiagnosisGroup;
import com.dangdang.entity.User;
import com.dangdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * [각주 BI] "틀려요, AI로 분석하기"를 눌렀을 때만 호출되는 서비스입니다.
 * FastAPI POST /rag/intake-logs/reanalyze로 그대로 프록시합니다 — IntakeLogService.recognizeFood()와
 * 원칙은 같은데(DB 저장 없음), 파일을 따로 둔 이유는 사용자가 정리한 구조(표)를 그대로 따른 겁니다.
 *
 * @lastModified 2026-08-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecognizeProxyService {

    private final FastApiClient fastApiClient;
    private final UserRepository userRepository;

    /**
     * image/foodName 중 최소 하나는 필수 — 검증은 컨트롤러에서 먼저 합니다.
     * diagnosisGroup 해석 규칙은 IntakeLogService.recognizeFood()의 [각주 V]와 동일합니다.
     */
    public ReanalyzeResponse reanalyze(Integer userNo, MultipartFile image, String foodName,
                                        Double baseline, String diagnosisGroup) {
        String resolvedDiagnosisGroup = (diagnosisGroup != null && !diagnosisGroup.isBlank())
                ? DiagnosisGroup.fromRawText(diagnosisGroup).getApiValue()
                : userRepository.findById(userNo).map(User::getDiagnosisGroup).orElse(null);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (image != null && !image.isEmpty()) {
            body.add("image", fastApiClient.toResource(image));
        }
        if (foodName != null && !foodName.isBlank()) {
            body.add("food_name", foodName);
        }
        if (baseline != null) {
            body.add("baseline", baseline);
        }
        if (resolvedDiagnosisGroup != null && !resolvedDiagnosisGroup.isBlank()) {
            body.add("diagnosis_group", resolvedDiagnosisGroup);
        }

        return fastApiClient.postMultipart(
                "/rag/intake-logs/reanalyze", body, ReanalyzeResponse.class
        );
    }
}
