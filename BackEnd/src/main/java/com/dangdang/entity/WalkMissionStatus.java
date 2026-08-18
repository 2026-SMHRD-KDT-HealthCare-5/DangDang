package com.dangdang.entity;

/**
 * [각주 AR] walk_mission.status 컬럼(varchar(20))에 대응되는 값입니다.
 * 실제 DB 스키마(DangDang_schema.md) 주석에 적힌 5가지 값 그대로 옮겼습니다.
 *
 * READY       : 미션이 막 생성됨 (아직 걷기 시작 전)
 * IN_PROGRESS : 사용자가 걷기 시작함
 * COMPLETE    : 목표 거리/칼로리 달성하고 정상 종료
 * PARTIAL     : 목표에는 못 미쳤지만 어느 정도 걷고 종료
 * EXPIRED     : 중간에 취소/만료됨 — 정확한 사유는 ExpireReason에 별도로 기록
 *
 * @lastModified 2026-08-18
 */
public enum WalkMissionStatus {
    READY,
    IN_PROGRESS,
    COMPLETE,
    PARTIAL,
    EXPIRED
}
