package com.dangdang.dto.response;

import java.math.BigDecimal;

/**
 * [각주 DD] POST /api/walk-missions/{mission_no}/post-glucose 응답입니다 (노션 "걷기 후 혈당 기록" 명세).
 *
 * preGlucose      : 식전 혈당 (intake_log.pre_glucose)
 * postGlucoseEst  : AI가 예상했던 식후 혈당 (intake_log.post_glucose_est)
 * postWalkGlucose : 방금 입력한 걷기 후 실측 혈당
 * targetDistance / actualDistance : 단위 m(미터)
 * goalAchieved    : 미션 status가 COMPLETE였는지 여부를 그대로 씁니다(별도 재계산 안 함 —
 *                    목표 도달 여부는 이미 /end에서 COMPLETE/PARTIAL로 확정돼 있으므로).
 * feedbackMessage : Gemini(LLM) 호출 없이 서버가 정해둔 고정 문구 — 필요한 4개 수치가
 *                    전부 서버가 이미 아는 값이라 굳이 AI를 부를 이유가 없습니다.
 *
 * @lastModified 2026-08-19
 */
public record PostGlucoseResponse(
        Integer missionNo,
        Integer preGlucose,
        Integer postGlucoseEst,
        Integer postWalkGlucose,
        BigDecimal targetDistance,
        BigDecimal actualDistance,
        boolean goalAchieved,
        String feedbackMessage
) {
}
