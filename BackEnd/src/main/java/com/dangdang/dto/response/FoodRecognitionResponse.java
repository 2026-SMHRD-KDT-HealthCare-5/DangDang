package com.dangdang.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * [음식인식이 식약청db에서 매칭이 됐을 때 ]
 * [각주 S] FastAPI POST /rag/intake-logs/recognize 가 돌려주는 JSON과 1:1로 대응되는 DTO입니다.
 * 필드 이름이 FastAPI 쪽과 정확히 같아야 Jackson(JSON <-> 자바 객체 변환기)이 자동으로
 * 값을 채워줍니다. 딱 하나(serving_size)만 FastAPI가 스네이크케이스로 보내서
 * @JsonProperty로 매핑해줬습니다.
 *
 * [각주] carb/sugar/protein/fat/fiber/calorie는 DB에서 DECIMAL(6,2) 타입입니다.
 * double은 부동소수점이라 미세한 반올림 오차가 생길 수 있어서, User 엔티티의 height/weight/hba1c와
 * 같은 이유로 BigDecimal을 씁니다 (정확한 십진수 계산이 필요한 값에는 BigDecimal이 표준입니다).
 * serving_size는 DB에서 INTEGER 타입이라 Integer로 받습니다.
 */
public record FoodRecognitionResponse(
        boolean matched,
        Integer foodNo,
        String foodName,
        @JsonProperty("serving_size") Integer servingSize,
        NutritionInfo nutrition,
        Double predictedGlucoseRise,
        String source,
        String chatbotMessage
) {
    /** food_info 테이블의 "1 serving_size 기준" 영양성분입니다 (100g 기준 아님. matched=false면 nutrition 전체가 null) */
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
