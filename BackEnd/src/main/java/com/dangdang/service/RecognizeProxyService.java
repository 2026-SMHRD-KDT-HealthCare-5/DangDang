package com.dangdang.service;

import com.dangdang.client.FastApiClient;
import com.dangdang.dto.response.ReanalyzeResponse;
import com.dangdang.entity.CustomFood;
import com.dangdang.entity.CustomFoodSource;
import com.dangdang.entity.User;
import com.dangdang.repository.CustomFoodRepository;
import com.dangdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * [각주 BI] "틀려요, AI로 분석하기"를 눌렀을 때만 호출되는 서비스입니다.
 * FastAPI POST /rag/intake-logs/reanalyze로 추정 결과를 받아온 다음, 여기서 바로
 * custom_food에 저장하고 그 PK(customFoodNo)를 같이 돌려줍니다.
 *
 * [각주] (수정 2026-08-25, 프론트 요청) 원래는 recognizeFood()와 동일하게 "DB 저장 없는
 * 순수 프록시"였는데, 프론트가 이 시점에 생긴 customFoodNo를 갖고 있어야 해서 저장하는
 * 걸로 바꿨습니다. ReanalyzeResponse 각주에 이 변경의 부작용(재분석 반복 시 고아 행 누적)을
 * 적어뒀습니다.
 *
 * @lastModified 2026-08-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecognizeProxyService {

    private final FastApiClient fastApiClient;
    private final UserRepository userRepository;
    private final CustomFoodRepository customFoodRepository;

    /**
     * image/foodName 중 최소 하나는 필수 — 검증은 컨트롤러에서 먼저 합니다.
     * diagnosisGroup은 요청으로 안 받고 항상 DB에서 조회합니다 —
     * IntakeLogService.recognizeFood()의 [각주 V]와 동일한 이유입니다.
     */
    @Transactional
    public ReanalyzeResponse reanalyze(Integer userNo, MultipartFile image, String foodName, Double baseline) {
        String resolvedDiagnosisGroup = userRepository.findById(userNo).map(User::getDiagnosisGroup).orElse(null);

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

        ReanalyzeResponse aiResult = fastApiClient.postMultipart(
                "/rag/intake-logs/reanalyze", body, ReanalyzeResponse.class
        );

        Integer customFoodNo = saveAsCustomFood(userNo, aiResult);

        return new ReanalyzeResponse(
                customFoodNo,
                aiResult.foodName(),
                aiResult.servingSize(),
                aiResult.nutrition(),
                aiResult.source(),
                aiResult.chatbotMessage()
        );
    }

    /**
     * [각주] (추가 2026-08-25, 수정 2026-08-25) FastAPI가 돌려준 추정치를 custom_food에
     * 그대로 저장합니다. source는 항상 "AI추정"으로 고정 — 이 메서드는 reanalyze() 경로에서만
     * 호출되기 때문입니다. nutrition이 통째로 null인 경우(FastAPI가 파싱 실패 등으로 값을 못 준
     * 극단적인 경우)는 저장 자체를 건너뛰고 customFoodNo를 null로 돌려줍니다.
     *
     * [각주 버그수정] FastAPI(food_recognition.py)의 reanalyze()는 carb/protein/fat/fiber만
     * "필수"로 검증하고 **sugar/calorie는 Gemini가 안 줘도 그냥 통과**시킵니다. 근데 custom_food
     * 테이블은 carb/sugar/calorie가 전부 NOT NULL이라, Gemini가 sugar나 calorie를 빼먹고
     * 응답하면 여기서 그대로 저장하려다가 "null value in column sugar violates not-null
     * constraint" 같은 DB 에러로 죽어서 reanalyze API 자체가 500으로 실패했습니다
     * (customFoodNo 추가 이전엔 이 메서드가 아예 없어서 문제가 안 됐던 부분). sugar/calorie가
     * null이면 0으로 대체해서 저장합니다 — "부정확한 AI 추정치"라는 특성상 0으로 대체해도
     * 치명적이지 않고, 사용자가 confirm 단계에서 최종 값을 다시 확인/수정할 기회가 있습니다.
     */
    private Integer saveAsCustomFood(Integer userNo, ReanalyzeResponse aiResult) {
        if (aiResult.nutrition() == null) {
            log.warn("reanalyze 결과에 nutrition이 없어 custom_food 저장을 건너뜁니다 (userNo={})", userNo);
            return null;
        }

        ReanalyzeResponse.NutritionInfo n = aiResult.nutrition();
        CustomFood customFood = CustomFood.builder()
                .userNo(userNo)
                .foodName(aiResult.foodName())
                .servingSize(aiResult.servingSize())
                .carb(zeroIfNull(n.carb()))
                .sugar(zeroIfNull(n.sugar()))
                .protein(n.protein())
                .fat(n.fat())
                .fiber(n.fiber())
                .calorie(zeroIfNull(n.calorie()))
                .source(CustomFoodSource.AI_ANALYSIS.getDbValue())
                .build();

        return customFoodRepository.save(customFood).getCustomFoodNo();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
