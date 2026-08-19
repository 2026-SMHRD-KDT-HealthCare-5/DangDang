package com.dangdang.dto.response;

/**
 * [각주 CP] POST /api/walk-missions/{mission_no}/track 응답입니다.
 *
 * goalReached      : 이번 폴링 기준으로 목표 거리(targetDistance)에 도달했는지
 * anomalyDetected  : 비정상 속도(이동 이상치)로 판단돼 이번 폴링의 거리 증가분이
 *                     반영되지 않았는지 — true면 프론트가 경고창만 띄우면 됩니다
 *                     (미션을 강제 종료하지는 않습니다 — 사용자 결정 사항).
 *
 * @lastModified 2026-08-19
 */
public record TrackResponse(
        boolean goalReached,
        boolean anomalyDetected
) {
}
