package com.dangdang.entity;

/**
 * [각주 AS] walk_mission.expire_reason 컬럼(varchar(20))에 대응되는 값입니다.
 * status가 EXPIRED일 때만 의미가 있고, "왜" 만료됐는지를 구분합니다. 노션 "API 기본 명세서" 6.2절과
 * 이름을 맞췄습니다 — 과거에는 CANCEL로 썼는데, 노션 표기(CANCELLED, L 두 개)로 통일했습니다.
 * (6.2절 표 헤더에는 CANCELED로 오타가 나 있지만, 상세 엔드포인트 페이지 2개와 표 바로 아래
 *  콜아웃은 전부 CANCELLED를 쓰고 있어서 다수 표기를 따랐습니다.)
 *
 * INACTIVE  : (추후 사용 예정) 서버 배치(매분) — IN_PROGRESS인데 last_tracked_at 30분 초과, 또는
 *             프론트가 POST /{mission_no}/expire로 30분간 위치 변화 없음을 감지해 직접 호출
 * TIMEOUT   : (추후 사용 예정) 서버 배치(매분) — READY 상태로 created_at 2시간 초과
 * CANCELLED : 두 가지 상황을 함께 씁니다.
 *   1) confirmIntake()에서, 사용자가 새 음식분석&걷기 확정을 진행하면서 이미 활성 상태(READY/IN_PROGRESS)였던
 *      이전 미션을 자동으로 취소하는 경우 (지금 이 값을 쓰는 유일한 실사용 케이스)
 *   2) (추후 사용 예정) 콜드스타트 시 프론트가 GET /api/walk-missions/active로 IN_PROGRESS를 발견했는데
 *      앱 로컬엔 진행 상태가 없어서 "연결이 끊겨서 종료됐어요"로 정리하는 경우
 *
 * @lastModified 2026-08-18
 */
public enum ExpireReason {
    INACTIVE,
    TIMEOUT,
    CANCELLED
}
