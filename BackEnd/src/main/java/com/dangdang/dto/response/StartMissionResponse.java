package com.dangdang.dto.response;

import com.dangdang.entity.WalkMissionStatus;

import java.time.LocalDateTime;

/**
 * [각주 CN] POST /api/walk-missions/{mission_no}/start 응답입니다 (노션 명세 그대로:
 * missionNo, status(IN_PROGRESS), startTime, createdAt).
 *
 * @lastModified 2026-08-19
 */
public record StartMissionResponse(
        Integer missionNo,
        WalkMissionStatus status,
        LocalDateTime startTime,
        LocalDateTime createdAt
) {
}
