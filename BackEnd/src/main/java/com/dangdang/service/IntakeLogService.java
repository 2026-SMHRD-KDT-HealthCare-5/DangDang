package com.dangdang.service;

import com.dangdang.dto.request.IntakeConfirmRequest;
import com.dangdang.dto.request.PortionPredictRequest;
import com.dangdang.dto.response.FoodRecognitionResponse;
import com.dangdang.dto.response.IntakeConfirmResponse;
import com.dangdang.dto.response.PortionPredictResponse;
import com.dangdang.dto.response.PreGlucoseResponse;
import com.dangdang.entity.AiChat;
import com.dangdang.entity.ChatType;
import com.dangdang.entity.CustomFood;
import com.dangdang.entity.CustomFoodSource;
import com.dangdang.entity.FoodInfo;
import com.dangdang.entity.IntakeLog;
import com.dangdang.entity.User;
import com.dangdang.entity.WalkMission;
import com.dangdang.entity.WalkMissionStatus;
import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import com.dangdang.repository.AiChatRepository;
import com.dangdang.repository.CustomFoodRepository;
import com.dangdang.repository.FoodInfoRepository;
import com.dangdang.repository.IntakeLogRepository;
import com.dangdang.repository.UserRepository;
import com.dangdang.repository.WalkMissionRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [각주 T] (수정) preglucose/recognize/predict는 FastAPI(AI 서버)로 그대로 전달(프록시)만 하고
 * DB에 아무것도 안 씁니다. 반면 confirmIntake()("맞아요" 최종 확정)만은 예외적으로 이 서비스
 * 안에서 직접 DB에 씁니다 — intake_log/custom_food/walk_mission/ai_chat(FOOD_CARD+MISSION_CARD).
 * (기획서 아키텍처 원칙: DB 쓰기는 전부 Spring, AI 추론은 전부 FastAPI가 전담하는데,
 *  "먹은 음식 최종 확정" 시점에야 DB에 저장하고, 인식/재분석 단계는 아직 저장할 대상이 없습니다.)
 *
 * [각주] (추가) FOOD_CARD는 recognize/reanalyze 시점이 아니라 여기서만 저장합니다(사용자 결정) —
 * 재검색/재분석을 몇 번 반복하든 대화 이력엔 최종 확정한 음식 1건만 남습니다.
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
    private final FoodInfoRepository foodInfoRepository;
    private final CustomFoodRepository customFoodRepository;
    private final IntakeLogRepository intakeLogRepository;
    private final WalkMissionRepository walkMissionRepository;
    private final AiChatRepository aiChatRepository;
    private final ObjectMapper objectMapper;
    // [각주 DB] 자동취소된 미션이 트래킹 캐시에 남아있지 않도록 정리하기 위해 주입합니다
    // (WalkMissionService -> IntakeLogService 방향 의존은 없어서 순환 걱정 없음).
    private final WalkMissionService walkMissionService;

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

    // [각주] FastAPI core/config.py의 DIAGNOSIS_GROUPS 기본값과 동일하게 맞춘 값입니다.
    private static final String DEFAULT_DIAGNOSIS_GROUP = "건강군";

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
     * [각주 V] (수정) diagnosisGroup은 더 이상 요청으로 안 받습니다 — 이건 사용자의 실제
     * 진단 정보라 요청마다 프론트가 고를 값이 아니라, 로그인한 사용자(userNo)의
     * users.diagnosis_group을 항상 그대로 조회해서 씁니다. 값이 없으면(진단군 미설정 회원)
     * null로 FastAPI에 전달되고, FastAPI가 기본값("건강군")을 적용합니다.
     *
     * (예전엔 프론트가 안드로이드 화면 문구를 rawText로 보내면 DiagnosisGroup.fromRawText()로
     * 검증+변환했는데, "제2형당뇨"를 "2형당뇨"로 잘못 보내는 등 입력 실수가 잦고 애초에 클라이언트가
     * 이 값을 바꿔 보낼 이유가 없어서 파라미터 자체를 없앴습니다. DB에서 꺼낸 값은 이미 FastAPI가
     * 이해하는 형식("건강군"/"전당뇨"/"2형당뇨")으로 저장돼 있어서 변환도 필요 없습니다.)
     */
    public FoodRecognitionResponse recognizeFood(Integer userNo, MultipartFile image, String message,
                                                  Double baseline) {
        String resolvedDiagnosisGroup = userRepository.findById(userNo).map(User::getDiagnosisGroup).orElse(null);

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
     * [각주 AN] POST /api/intake-logs/predict 가 호출합니다.
     * 대화 흐름상 recognize 다음 단계 — "얼마나 드셨어요?"(portion)에 답하면 이걸 호출해서
     * 1인분 영양성분 x portion 반영된 예상 혈당 상승량/걷기 미션 목표치를 미리 보여줍니다.
     * (아직 "맞아요" 최종 확정 전이라 DB에는 저장하지 않습니다 — recognizeFood와 같은 순수 프록시)
     *
     * FastAPI schemas/predict.py의 PortionPredictRequest는 baseline/diagnosis_group이
     * 둘 다 필수(기본값 없음)입니다. recognize와 달리 FastAPI가 자동으로 기본값을 채워주지
     * 않으므로, Spring이 여기서 직접 확실한 값을 만들어 보내야 합니다.
     *
     * [각주] (수정) diagnosisGroup도 recognizeFood()와 동일하게 요청으로 안 받고 DB에서
     * 항상 조회합니다 — [각주 V] 참고.
     */
    public PortionPredictResponse predictPortion(Integer userNo, PortionPredictRequest request) {
        if (request.baseline() == null) {
            throw new BusinessException(ErrorCode.MISSING_BASELINE);
        }

        String resolvedDiagnosisGroup = userRepository.findById(userNo).map(User::getDiagnosisGroup).orElse(null);
        if (resolvedDiagnosisGroup == null || resolvedDiagnosisGroup.isBlank()) {
            // [각주] recognize는 이 경우 null을 보내면 FastAPI가 알아서 "건강군"으로 채워주지만,
            // predict.py는 diagnosis_group이 DIAGNOSIS_GROUPS(건강군/전당뇨/2형당뇨) 안에 없으면
            // 그냥 400을 돌려줍니다 — 그래서 여기서 Spring이 직접 기본값을 정해서 보냅니다.
            resolvedDiagnosisGroup = DEFAULT_DIAGNOSIS_GROUP;
        }

        FastApiPredictRequest fastApiRequest = new FastApiPredictRequest(
                request.carb(), request.sugar(), request.protein(), request.fat(),
                request.fiber(), request.calorie(),
                request.portion() != null ? request.portion() : 1.0,
                request.baseline(), resolvedDiagnosisGroup
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Api-Key", internalApiKey);
        HttpEntity<FastApiPredictRequest> requestEntity = new HttpEntity<>(fastApiRequest, headers);

        try {
            return restTemplate.postForObject(
                    fastApiBaseUrl + "/rag/intake-logs/predict",
                    requestEntity,
                    PortionPredictResponse.class
            );
        } catch (RestClientException e) {
            log.error("FastAPI 호출 실패 (predict): {}", e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }
    }

    /**
     * [각주 BN] POST /api/intake-logs (최종 확정, "맞아요") 가 호출합니다. 여기서만 실제로
     * DB에 씁니다 — intake_log / (필요하면) custom_food / walk_mission / ai_chat(MISSION_CARD)
     * 를 한 트랜잭션으로 같이 저장합니다. 중간에 하나라도 실패하면 전부 롤백됩니다.
     *
     * 순서:
     * 1) foodNo XOR customFood 검증 (intake_log의 chk_food_reference와 동일한 규칙)
     * 2) preGlucose 해결 (recognize와 동일 — 프론트가 안 보내면 hba1c 기본값)
     * 3) food_no 경로면 food_info에서, customFood 경로면 지금 막 저장한 custom_food에서
     *    "서버가 신뢰하는" 영양성분을 다시 확보 (프론트가 보낸 값을 그대로 믿지 않음)
     * 4) FastAPI predict를 다시 호출해서 predictedGlucoseRise/targetDistance/targetKcal 재계산
     *    (프론트가 /predict로 이미 받은 값을 또 보내더라도 그건 무시하고 서버가 새로 계산합니다)
     * 5) 이미 활성 미션(READY/IN_PROGRESS)이 있으면 자동 취소(EXPIRED/CANCELLED) 후 진행
     *    — uq_active_mission(부분 유니크 인덱스)이 "유저당 활성 미션 1개"를 DB 레벨에서도 강제합니다
     * 6) intake_log 저장 → walk_mission 저장 → ai_chat(MISSION_CARD) 저장
     *
     * @lastModified 2026-08-18
     */
    @Transactional
    public IntakeConfirmResponse confirmIntake(Integer userNo, IntakeConfirmRequest request) {
        validateFoodReference(request);

        PreGlucoseResponse resolvedPreGlucose = resolvePreGlucose(userNo, request.preGlucose());
        Double baseline = resolvedPreGlucose.preGlucose().doubleValue();

        Integer foodNo = null;
        Integer customFoodNo = null;
        BigDecimal carb, sugar, protein, fat, fiber, calorie;
        double portion;
        String foodName;
        Integer servingSize;
        String source;

        if (request.foodNo() != null) {
            // --- "맞아요" (식약처 매칭 그대로 확정) ---
            FoodInfo foodInfo = foodInfoRepository.findById(request.foodNo())
                    .orElseThrow(() -> new BusinessException(ErrorCode.FOOD_NOT_FOUND));
            foodNo = foodInfo.getFoodNo();
            carb = foodInfo.getCarb();
            sugar = foodInfo.getSugar();
            protein = foodInfo.getProtein();
            fat = foodInfo.getFat();
            fiber = foodInfo.getFiber();
            calorie = foodInfo.getCalorie();
            portion = request.portion() != null ? request.portion() : 1.0;
            foodName = foodInfo.getFoodName();
            servingSize = foodInfo.getServingSize();
            source = "공공데이터";
        } else {
            // --- "틀려요→AI로 분석하기" / "직접입력하기" 확정 ---
            // [각주 BJ-1] CustomFood 저장은 별도 서비스로 안 빼고 여기서 직접 처리합니다
            // (한때 FoodService에 뒀었는데, 순환 의존 문제 + "직접입력은 미리보기 없이 confirm
            //  한 번에 끝낸다"는 결정 이후로 FoodService 자체를 삭제했습니다).
            IntakeConfirmRequest.CustomFoodPayload payload = request.customFood();
            String validatedSource = CustomFoodSource.validateDbValue(payload.source());

            CustomFood customFood = CustomFood.builder()
                    .userNo(userNo)
                    .foodName(payload.foodName())
                    .servingSize(payload.servingSize())
                    .carb(payload.carb())
                    .sugar(payload.sugar())
                    .protein(payload.protein())
                    .fat(payload.fat())
                    .fiber(payload.fiber())
                    .calorie(payload.calorie())
                    .source(validatedSource)
                    .build();
            CustomFood savedCustomFood = customFoodRepository.save(customFood);

            customFoodNo = savedCustomFood.getCustomFoodNo();
            carb = savedCustomFood.getCarb();
            sugar = savedCustomFood.getSugar();
            protein = savedCustomFood.getProtein();
            fat = savedCustomFood.getFat();
            fiber = savedCustomFood.getFiber();
            calorie = savedCustomFood.getCalorie();
            // [각주 BK] 직접입력/AI추정 값은 "1인분 기준"이 아니라 "실제 먹은 양 그 자체"라 portion 고정 1.0.
            portion = 1.0;
            foodName = savedCustomFood.getFoodName();
            servingSize = savedCustomFood.getServingSize();
            source = savedCustomFood.getSource();
        }

        PortionPredictRequest predictRequest = new PortionPredictRequest(
                carb, sugar, protein, fat, fiber, calorie,
                portion, baseline
        );
        PortionPredictResponse predicted = predictPortion(userNo, predictRequest);

        // [각주 BO] (시나리오 2: 사용자 답변) 이미 활성 미션이 있는데 또 확정을 시도하면,
        // 새 확정을 거부하지 않고 기존 것을 자동 취소한 뒤 새 미션으로 진행합니다.
        // (시나리오 1 — 걷기 도중 앱 종료 후 콜드스타트에서 발견되는 경우는 별도의
        //  "/api/walk-missions/active" 콜드스타트 체크 API에서 처리할 예정 — 이번 범위 밖)
        walkMissionRepository.findFirstByUserNoAndStatusIn(
                userNo, List.of(WalkMissionStatus.READY, WalkMissionStatus.IN_PROGRESS)
        ).ifPresent(activeMission -> {
            activeMission.cancelForNewConfirm();
            // [각주 CN] save()가 아니라 saveAndFlush()를 씁니다 — 이유: WalkMission의 PK는
            // GenerationType.IDENTITY라, 바로 아래에서 "새" WalkMission을 save()하는 순간
            // (트랜잭션 끝까지 안 미뤄지고) INSERT가 그 즉시 DB로 나갑니다(IDENTITY는 DB가
            // 키를 만들어주는 방식이라 Hibernate가 그 값을 바로 받아와야 해서 미룰 수가 없음).
            // 반면 이 UPDATE(취소 처리)는 그냥 save()만 쓰면 트랜잭션 커밋 시점까지 미뤄지는게
            // 기본 동작이라, "이전 미션을 취소 → 새 미션 저장"을 코드 순서대로 짰어도 실제 DB에는
            // "새 미션 INSERT"가 "이전 미션 취소 UPDATE"보다 먼저 도착해버렸습니다. 그 순간 DB에는
            // 같은 유저의 활성(READY/IN_PROGRESS) 미션이 일시적으로 2건이 되어버려서
            // uq_active_mission(유저당 활성 미션 1개 제약, 부분 유니크 인덱스)에 걸려
            // "중복" 에러가 났던 겁니다. saveAndFlush()로 취소 UPDATE를 먼저 확실히 DB에
            // 반영해두면 이 순서 역전이 사라집니다.
            walkMissionRepository.saveAndFlush(activeMission);
            walkMissionService.evictCheckpointCache(activeMission.getMissionNo());
        });

        Integer postGlucoseEst = (predicted.predictedGlucoseRise() != null)
                ? (int) Math.round(baseline + predicted.predictedGlucoseRise())
                : null;

        IntakeLog intakeLog = IntakeLog.builder()
                .userNo(userNo)
                .foodNo(foodNo)
                .customFoodNo(customFoodNo)
                .preGlucose(resolvedPreGlucose.preGlucose())
                .postGlucoseEst(postGlucoseEst)
                .portion(BigDecimal.valueOf(portion))
                .build();
        IntakeLog savedLog = intakeLogRepository.save(intakeLog);

        // [각주 DG] FOOD_CARD는 recognize/reanalyze 시점이 아니라 여기(확정 시점)에만 저장합니다
        // — 재검색/재분석을 몇 번을 반복하든 대화 이력에는 최종 확정한 음식 1건만 남기기로
        // 결정했습니다(사용자 결정 사항). recognize/reanalyze는 여전히 DB에 아무것도 안 씁니다.
        // 이 시점엔 predictPortion()이 이미 끝나서 portion 반영된 정확한 predictedGlucoseRise를
        // 알고 있으니, recognize 때와 달리 부정확한 값 문제도 없습니다.
        saveFoodCardChat(userNo, savedLog, foodNo, customFoodNo, foodName, servingSize, source,
                carb, sugar, protein, fat, fiber, calorie, predicted.predictedGlucoseRise());

        WalkMission mission = WalkMission.builder()
                .userNo(userNo)
                .logNo(savedLog.getLogNo())
                .targetKcal(predicted.targetKcal() != null ? BigDecimal.valueOf(predicted.targetKcal()) : BigDecimal.ZERO)
                .targetDistance(predicted.targetDistance() != null ? BigDecimal.valueOf(predicted.targetDistance()) : BigDecimal.ZERO)
                .build();
        WalkMission savedMission = walkMissionRepository.save(mission);

        String chatbotMessage = "기록할 음식이 확정되었어요! 이제 식후 30분 이후 걷기를 시작해볼까요?";
        saveMissionCardChat(userNo, savedLog, savedMission, predicted.targetTimeMinutes(), chatbotMessage);

        return new IntakeConfirmResponse(
                savedLog.getLogNo(),
                savedMission.getMissionNo(),
                predicted.predictedGlucoseRise(),
                savedMission.getTargetDistance(),
                savedMission.getTargetKcal(),
                predicted.targetTimeMinutes(),
                chatbotMessage
        );
    }

    /** intake_log의 chk_food_reference와 정확히 같은 규칙을 자바 코드에서 먼저 검증합니다. */
    private void validateFoodReference(IntakeConfirmRequest request) {
        boolean hasFoodNo = request.foodNo() != null;
        boolean hasCustomFood = request.customFood() != null;
        if (hasFoodNo == hasCustomFood) { // 둘 다 true(둘 다 옴) 이거나 둘 다 false(둘 다 없음)면 에러
            throw new BusinessException(ErrorCode.INVALID_CONFIRM_FOOD_REFERENCE);
        }
    }

    /**
     * [각주 DH] FOOD_CARD 타입 ai_chat row를 저장합니다 — "확정된 음식" 1건의 스냅샷입니다.
     * foodNo/customFoodNo 중 확정 시 실제로 채워진 쪽만 값이 들어가고 나머지는 null입니다
     * (intake_log의 chk_food_reference와 동일한 XOR 규칙).
     */
    private void saveFoodCardChat(Integer userNo, IntakeLog intakeLog, Integer foodNo, Integer customFoodNo,
                                   String foodName, Integer servingSize, String source,
                                   BigDecimal carb, BigDecimal sugar, BigDecimal protein,
                                   BigDecimal fat, BigDecimal fiber, BigDecimal calorie,
                                   Double predictedGlucoseRise) {
        Map<String, Object> nutrition = new LinkedHashMap<>();
        nutrition.put("carb", carb);
        nutrition.put("sugar", sugar);
        nutrition.put("protein", protein);
        nutrition.put("fat", fat);
        nutrition.put("fiber", fiber);
        nutrition.put("calorie", calorie);

        Map<String, Object> card = new LinkedHashMap<>();
        card.put("logNo", intakeLog.getLogNo());
        card.put("foodNo", foodNo);
        card.put("customFoodNo", customFoodNo);
        card.put("foodName", foodName);
        card.put("servingSize", servingSize);
        card.put("nutrition", nutrition);
        card.put("predictedGlucoseRise", predictedGlucoseRise);
        card.put("source", source);

        String cardDataJson;
        try {
            cardDataJson = objectMapper.writeValueAsString(card);
        } catch (JsonProcessingException e) {
            log.error("FOOD_CARD cardData 직렬화 실패 (logNo={}): {}", intakeLog.getLogNo(), e.getMessage());
            cardDataJson = null;
        }

        AiChat aiChat = AiChat.builder()
                .userNo(userNo)
                .aiMessage(foodName + " 기록 완료!")
                .chatType(ChatType.FOOD_CARD)
                .cardData(cardDataJson)
                .build();
        aiChatRepository.save(aiChat);
    }

    /**
     * MISSION_CARD 타입 ai_chat row를 저장합니다. cardData는 프론트가 나중에 이력을 다시 그릴 때 쓸 스냅샷입니다.
     *
     * [각주] targetTimeMinutes는 WalkMission 엔티티(DB)에는 저장하지 않습니다 — walk_mission
     * 테이블에 이 값을 담을 컬럼이 없어서(target_distance/target_kcal만 있음), 여기 카드
     * 스냅샷에만 값을 남겨둡니다. 그래서 이 값은 지금 이 확정 응답/카드에서만 볼 수 있고,
     * 나중에 GET /api/walk-missions/active로 다시 조회할 때는 안 나옵니다 — 필요하면
     * walk_mission에 컬럼을 추가해야 합니다(DB는 직접 관리하시니, 필요하면 알려드릴게요).
     */
    private void saveMissionCardChat(Integer userNo, IntakeLog intakeLog, WalkMission mission,
                                      Integer targetTimeMinutes, String chatbotMessage) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("logNo", intakeLog.getLogNo());
        card.put("missionNo", mission.getMissionNo());
        card.put("targetDistance", mission.getTargetDistance());
        card.put("targetKcal", mission.getTargetKcal());
        card.put("targetTimeMinutes", targetTimeMinutes);

        String cardDataJson;
        try {
            cardDataJson = objectMapper.writeValueAsString(card);
        } catch (JsonProcessingException e) {
            // [각주] MISSION_CARD 표시용 스냅샷이 실패해도 확정 자체(intake_log/walk_mission 저장)는
            // 막을 이유가 없어서, cardData만 null로 두고 넘어갑니다(로그만 남김).
            log.error("MISSION_CARD cardData 직렬화 실패 (logNo={}): {}", intakeLog.getLogNo(), e.getMessage());
            cardDataJson = null;
        }

        AiChat aiChat = AiChat.builder()
                .userNo(userNo)
                .aiMessage(chatbotMessage)
                .chatType(ChatType.MISSION_CARD)
                .cardData(cardDataJson)
                .build();
        aiChatRepository.save(aiChat);
    }

    /**
     * FastAPI schemas/predict.py의 PortionPredictRequest와 맞춘 내부 전용 요청 형식입니다.
     * ChatService의 [각주 AG]와 동일한 이유로 diagnosis_group만 스네이크케이스 매핑이 필요합니다.
     */
    private record FastApiPredictRequest(
            BigDecimal carb, BigDecimal sugar, BigDecimal protein, BigDecimal fat,
            BigDecimal fiber, BigDecimal calorie, Double portion, Double baseline,
            @JsonProperty("diagnosis_group") String diagnosisGroup
    ) {
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
