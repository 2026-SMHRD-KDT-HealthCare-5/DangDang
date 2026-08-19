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
 * [각주] (수정) predictedGlucoseRise는 뺐습니다 — 이 시점엔 portion을 몰라서 1인분 가정으로만
 * 계산되는 부정확한 값이었고, 어차피 "얼마나 드셨어요?" 응답 후 /predict가 실제 값을 다시
 * 계산해서 줍니다. FastAPI(food_recognition.py)도 이 계산(모델 호출) 자체를 없앴습니다.
 *
 * @lastModified 2026-08-19
 */
public record ReanalyzeResponse(
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
