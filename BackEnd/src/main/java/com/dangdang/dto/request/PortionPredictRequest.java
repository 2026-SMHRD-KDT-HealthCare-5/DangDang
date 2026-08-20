package com.dangdang.dto.request;

import java.math.BigDecimal;

/**
 * [각주 AM] POST /api/intake-logs/predict 요청 바디.
 *
 * 대화 흐름: preglucose(식전혈당) → recognize(음식 인식, 1인분 기준 영양성분 응답) →
 * "얼마나 드셨어요?"(portion) → 이 API. 그래서 carb~calorie 값은 새로 조회하는 게 아니라,
 * 프론트가 recognize 응답(FoodRecognitionResponse.nutrition)에서 이미 받아둔 값을
 * 그대로 다시 보내는 것입니다 — preGlucose와 같은 패턴(서버가 중간 상태를 안 들고 있음).
 *
 * baseline은 필수입니다 — preglucose 단계에서 이미 정해진 값이라 여기서 새로 기본값을
 * 계산하지 않습니다(그건 FastAPI predict.py도 마찬가지로 필수 값으로 받습니다).
 *
 * [각주] (수정) diagnosisGroup 필드를 뺐습니다 — 이 값은 사용자의 실제 진단 정보라
 * 요청마다 프론트가 골라 보낼 이유가 없고, 서버가 항상 users.diagnosis_group을 그대로
 * 조회해서 씁니다(IntakeLogService.predictPortion() 참고). 클라이언트가 rawText 형식
 * ("제2형당뇨" 등)을 잘못 보내서 검증 에러가 나는 문제도 이걸로 원천 차단됩니다.
 */
public record PortionPredictRequest(
        BigDecimal carb,
        BigDecimal sugar,
        BigDecimal protein,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal calorie,
        Double portion,
        Double baseline
) {
}
