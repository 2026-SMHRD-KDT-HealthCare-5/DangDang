package com.dangdang.dto.response;

import com.dangdang.entity.WalkMissionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * [각주 BV] GET /api/walk-missions/active 응답입니다. 앱 콜드스타트 시 호출해서
 * "지금 이 유저에게 진행 중이던 걷기 미션이 있는지" 복구 여부를 판단하는 데 씁니다.
 *
 * 활성 미션(READY/IN_PROGRESS)이 없으면 컨트롤러가 이 DTO 대신 204 No Content를 돌려줍니다
 * (노션 "진행 중인 미션 조회" 명세 그대로).
 *
 * [각주] targetDistance/actualDistance 단위는 m(미터)입니다.
 *
 * @lastModified 2026-08-19
 */
public record ActiveMissionResponse(
        Integer missionNo,
        WalkMissionStatus status,
        BigDecimal targetDistance,
        BigDecimal targetKcal,
        BigDecimal actualDistance,
        LocalDateTime startTime,
        LocalDateTime lastTrackedAt,
        LocalDateTime createdAt
) {
}
