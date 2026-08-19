package com.dangdang.dto.response;

import com.dangdang.entity.WalkMissionStatus;

import java.math.BigDecimal;

/**
 * [각주 CQ] POST /api/walk-missions/{mission_no}/end (수동 종료) 응답입니다.
 *
 * actualDistance   : 실제 이동거리, 단위 m(미터)
 * durationMinutes  : startTime부터 endTime까지 걸린 시간(분)
 * burnedKcal       : MET(운동대사당량) 공식 기반 추정 소모 칼로리 — WalkMissionService 참고
 * postGlucosePrompted : 챗봇에 POST_GLUCOSE 카드가 함께 저장됐음을 프론트에 알리는 플래그
 *
 * @lastModified 2026-08-19
 */
public record EndMissionResponse(
        WalkMissionStatus status,
        BigDecimal actualDistance,
        Long durationMinutes,
        BigDecimal burnedKcal,
        boolean postGlucosePrompted
) {
}
