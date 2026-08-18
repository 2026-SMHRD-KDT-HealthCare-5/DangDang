package com.dangdang.dto.response;

import java.math.BigDecimal;

/**
 * [각주 BG] POST /api/intake-logs (최종 확정, "맞아요") 응답입니다.
 * intake_log + walk_mission 저장이 끝난 뒤, 프론트가 MISSION_CARD(걷기 챌린지 추천 카드)를
 * 바로 그릴 수 있게 필요한 값을 전부 담아 돌려줍니다.
 *
 * @lastModified 2026-08-18
 */
public record IntakeConfirmResponse(
        Integer logNo,
        Integer missionNo,
        Double predictedGlucoseRise,
        BigDecimal targetDistance,
        BigDecimal targetKcal,
        String chatbotMessage
) {
}
