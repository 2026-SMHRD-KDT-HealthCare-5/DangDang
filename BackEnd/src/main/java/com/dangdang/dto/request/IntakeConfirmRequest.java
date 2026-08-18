package com.dangdang.dto.request;

import java.math.BigDecimal;

/**
 * [각주 BC] POST /api/intake-logs (최종 확정, "맞아요") 요청 바디입니다.
 *
 * foodNo와 customFood 중 정확히 하나만 와야 합니다 (intake_log의 chk_food_reference 제약과 동일한 규칙).
 * - foodNo만 옴        : 식약처 DB 매칭 그대로 확정 ("맞아요" 버튼, recognize 결과 그대로)
 * - customFood만 옴    : "틀려요→AI로 분석하기" 또는 "직접입력하기"로 나온 값을 확정
 *                        (source 값으로 둘을 구분 — "AI추정" / "사용자입력")
 *
 * portion은 foodNo 경로에서만 의미가 있습니다 (1인분 기준 영양성분 x portion).
 * customFood 경로는 사용자가 입력/추정된 값 자체가 "실제로 먹은 양"이라, 서버가 portion을
 * 항상 1.0으로 고정 처리합니다(요청에 값을 보내도 무시됩니다) — service 쪽 주석 참고.
 *
 * @lastModified 2026-08-18
 */
public record IntakeConfirmRequest(
        Integer foodNo,
        CustomFoodPayload customFood,
        Integer preGlucose,
        Double portion,
        String diagnosisGroup
) {
    /** "틀려요→AI로 분석하기" 또는 "직접입력하기" 단계에서 나온 음식 정보를 그대로 실어 보냅니다. */
    public record CustomFoodPayload(
            String foodName,
            Integer servingSize,
            BigDecimal carb,
            BigDecimal sugar,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal fiber,
            BigDecimal calorie,
            // "AI추정" / "사용자입력" — CustomFoodSource.getDbValue()와 정확히 같은 문자열이어야 합니다.
            String source
    ) {
    }
}
