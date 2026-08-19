package com.dangdang.dto.request;

/**
 * [각주 DC] POST /api/walk-missions/{mission_no}/post-glucose 요청 바디입니다.
 * postWalkGlucose : 걷기 후 실측 혈당(mg/dL, 정수). 노션 명세 그대로 이 필드 하나뿐입니다 —
 * missionNo는 URL 경로에서, userNo는 JWT에서 각각 가져오니 body엔 안 넣습니다.
 *
 * @lastModified 2026-08-19
 */
public record PostGlucoseRequest(
        Integer postWalkGlucose
) {
}
