package com.dangdang.entity;

/**
 * [각주 AS] walk_mission.expire_reason 컬럼(varchar(20))에 대응되는 값입니다.
 * status가 EXPIRED일 때만 의미가 있고, "왜" 만료됐는지를 구분합니다.
 *
 * INACTIVE : (기존 스키마 주석 기준, 이번 작업에서는 안 씀 — 추후 배치/타임아웃 감지 로직에서 사용 예정)
 * TIMEOUT  : (위와 동일 — 추후 사용 예정)
 * CANCEL   : 사용자가 새로운 음식분석&걷기 확정을 진행하면서, 이미 활성 상태(READY/IN_PROGRESS)였던
 *            이전 미션이 자동으로 취소된 경우. confirmIntake()가 이 값을 씁니다.
 *            (콜드스타트 시 "연결이 끊겨서 종료됐어요" 케이스도 이 값을 재사용할 예정 — 다음 작업)
 *
 * @lastModified 2026-08-18
 */
public enum ExpireReason {
    INACTIVE,
    TIMEOUT,
    CANCEL
}
