package com.dangdang.dto.request;

/**
 * [각주 FH] PATCH /api/users/me/notification 요청 바디입니다.
 * notification_enabled 토글 — 끄면 선제적 안부 알림(식사 시간대 · 장시간 미활동)을 생성하지 않습니다.
 *
 * @lastModified 2026-08-21
 */
public record NotificationUpdateRequest(
        Boolean notificationEnabled
) {
}
