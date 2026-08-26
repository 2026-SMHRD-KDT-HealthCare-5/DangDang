package com.dangdang.dto.response;

import com.dangdang.entity.ChatType;

/**
 * [각주 AD] POST /api/chat 응답. 노션 명세 기준 aiChatNo/aiMessage/chatType(항상 TEXT)만 돌려줍니다.
 * userMessage는 안 돌려주는데 — 어차피 요청으로 보낸 값을 프론트가 이미 들고 있어서 중복이기 때문입니다.
 */
public record ChatResponse(
        Integer aiChatNo,
        String aiMessage,
        ChatType chatType
) {
}
