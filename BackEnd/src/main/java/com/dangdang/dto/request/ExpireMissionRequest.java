package com.dangdang.dto.request;

import java.math.BigDecimal;

/**
 * [각주 BW] POST /api/walk-missions/{mission_no}/expire 요청 바디입니다.
 * 프론트가 두 가지 상황에서 직접 호출합니다 (노션 "걷기 자동 종료" 명세):
 * - expireReason="INACTIVE"  : 앱이 실행 중인데 30분간 위치 변화가 없음을 감지했을 때
 * - expireReason="CANCELLED" : 콜드스타트 때 GET /active가 IN_PROGRESS를 반환했는데
 *                              앱 로컬엔 진행 상태가 없을 때 (죽었다 다시 켜진 경우)
 *
 * actualDistance는 선택값입니다 — INACTIVE는 앱이 그동안 추적한 실측 거리를 실어 보낼 수 있고,
 * CANCELLED는 이어하기를 지원하지 않아 보통 null(또는 0)로 옵니다. 단위는 m(미터)입니다.
 *
 * @lastModified 2026-08-19
 */
public record ExpireMissionRequest(
        String expireReason,
        BigDecimal actualDistance
) {
}
