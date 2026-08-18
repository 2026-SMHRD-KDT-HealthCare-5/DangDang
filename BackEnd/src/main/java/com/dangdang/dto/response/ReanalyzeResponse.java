package com.dangdang.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * [각주 BE] FastAPI POST /rag/intake-logs/reanalyze 응답과 1:1 대응되는 DTO입니다.
 * "틀려요, AI로 분석하기"를 눌렀을 때만 호출됩니다 — DB 저장은 없습니다(순수 프록시,
 * recognizeFood와 동일한 원칙). 여기서 나온 값은 프론트가 메모리에 들고 있다가,
 * 사용자가 "맞아요"를 누르면 IntakeConfirmRequest.customFood로 그대로 실어 보내면 됩니다
 * (이때 source는 "AI추정"으로 고정해서 보내면 됩니다).
 *
 * @lastModified 2026-08-18
 */
public record ReanalyzeResponse(
        String foodName,
        @JsonProperty("serving_size") Integer servingSize,
        NutritionInfo nutrition,
        Double predictedGlucoseRise,
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
