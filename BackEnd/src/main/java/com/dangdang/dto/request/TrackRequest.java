package com.dangdang.dto.request;

import java.math.BigDecimal;

/**
 * [각주 CO] POST /api/walk-missions/{mission_no}/track 요청 바디입니다. 프론트가 30초마다
 * 호출합니다. latitude/longitude는 지금 로직에선 안 쓰지만(거리 계산은 프론트가 이미 끝내서
 * currentDistance로 보내줌), 나중을 위해 그대로 받아둡니다.
 *
 * currentDistance : 미션 시작부터 지금까지의 누적 이동거리(m, 미터). 폴링마다 리셋되지 않고
 * 계속 커지기만 하는 값입니다 — WalkMissionService.trackMission() 참고.
 *
 * [각주] (수정) 단위를 km → m로 바꿨습니다. WalkMissionService의 체크포인트 최소 이동거리(1m)
 * /이상치 속도(초당 4.5m) 판정이 원래부터 "미터" 단위를 가정하고 있었는데, 예전엔 여기로
 * km 값이 들어오고 있어서 단위가 안 맞았던 걸 바로잡았습니다.
 *
 * @lastModified 2026-08-19
 */
public record TrackRequest(
        Double latitude,
        Double longitude,
        BigDecimal currentDistance
) {
}
