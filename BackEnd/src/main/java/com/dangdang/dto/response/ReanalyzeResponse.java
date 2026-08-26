package com.dangdang.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * [각주 BE] FastAPI POST /rag/intake-logs/reanalyze 응답 + customFoodNo를 합친 DTO입니다.
 * "틀려요, AI로 분석하기"를 눌렀을 때만 호출됩니다.
 *
 * [각주] (수정 2026-08-25) customFoodNo 필드를 추가했습니다 — 프론트 팀 요청으로, 이 시점의
 * 추정 결과를 custom_food 테이블에 바로 저장하고 그 PK를 돌려줍니다(RecognizeProxyService
 * 참고). **주의**: 이전엔 "재분석/직접입력 단계는 '맞아요' 누르기 전까진 DB에 아무것도 안 쓴다"는
 * 원칙이었는데(CustomFood 엔티티 각주 AW 참고), 이 변경으로 그 원칙이 깨졌습니다 — 사용자가
 * "틀려요, 다시 분석"을 여러 번 누르면 그때마다 custom_food에 새 행이 하나씩 쌓이고,
 * 그중 실제로 확정(confirm)되는 건 최대 1건뿐이라 나머지는 "미확정 미리보기" 상태로 테이블에
 * 계속 남습니다(고아 행). 데이터가 많이 쌓이면 나중에 정리가 필요할 수 있는데, 그 정리는
 * 사용자님이 DB를 직접 관리하시는 부분이라 필요해지면 정리 방법만 알려드리겠습니다.
 *
 * [각주] (수정) predictedGlucoseRise는 뺐습니다 — 이 시점엔 portion을 몰라서 1인분 가정으로만
 * 계산되는 부정확한 값이었고, 어차피 "얼마나 드셨어요?" 응답 후 /predict가 실제 값을 다시
 * 계산해서 줍니다. FastAPI(food_recognition.py)도 이 계산(모델 호출) 자체를 없앴습니다.
 *
 * @lastModified 2026-08-25
 */
public record ReanalyzeResponse(
        @JsonProperty("customFoodNo") Integer customFoodNo,
        String foodName,
        @JsonProperty("serving_size") Integer servingSize,
        NutritionInfo nutrition,
        String source,
        String chatbotMessage
) {
    public record NutritionInfo(
            BigDecimal carb,
            BigDecimal sugar,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal fiber,
            BigDecimal calorie
    ) {
    }
}
