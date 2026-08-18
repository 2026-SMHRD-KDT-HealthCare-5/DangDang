package com.dangdang.dto.response;

import java.math.BigDecimal;

/**
 * [각주 AM] POST /api/intake-logs/predict 응답. FastAPI POST /rag/intake-logs/predict의
 * 응답을 그대로 옮긴 형태입니다 — portion이 반영된(곱해진) 최종 영양성분(nutritionUsed)과
 * 그걸로 예측한 혈당 상승량, 걷기 미션 목표치까지 한 번에 옵니다.
 *
 * 이 단계에서는 아직 DB에 저장하지 않습니다("맞아요" 최종 확정 때 저장 — 아직 미구현).
 */
public record PortionPredictResponse(
        Double predictedGlucoseRise,
        Double predictedPeak,
        Double targetDistance,
        Double targetKcal,
        NutritionUsed nutritionUsed
) {
    public record NutritionUsed(
            BigDecimal carb,
            BigDecimal sugar,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal fiber,
            BigDecimal calorie
    ) {
    }
}
