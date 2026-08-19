package com.dangdang.dto.response;

import com.dangdang.entity.ExpireReason;
import com.dangdang.entity.WalkMissionStatus;

/**
 * [각주 BX] POST /api/walk-missions/{mission_no}/expire 응답입니다 (노션 명세: status,
 * expireReason, noticeMessage). noticeMessage는 챗봇에도 NOTICE로 그대로 저장되는 문구입니다.
 *
 * @lastModified 2026-08-18
 */
public record ExpireMissionResponse(
        WalkMissionStatus status,
        ExpireReason expireReason,
        String noticeMessage
) {
}
