package com.dangdang.dto.response;

import com.dangdang.entity.ChatType;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [각주 AE] GET /api/chat/history 응답. 노션 명세의 { date, messages: [...] } 형태 그대로입니다.
 */
public record ChatHistoryResponse(
        String date,
        List<ChatMessage> messages
) {
    /**
     * cardData를 JsonNode로 받는 이유 — DB엔 JSON을 문자열로 저장해뒀지만(AiChat.cardData),
     * 응답 JSON에 그 문자열을 그대로 넣으면 "escaped 문자열"(예: "{\"foodName\":...}")로
     * 이중 직렬화가 돼버립니다. JsonNode로 파싱해서 넣어야 진짜 JSON 객체로(예시 응답처럼
     * cardData: {"foodName": ...}) 내려갑니다. TEXT/NOTICE/PRE_GLUCOSE는 null.
     */
    public record ChatMessage(
            Integer aiChatNo,
            String userMessage,
            String aiMessage,
            ChatType chatType,
            JsonNode cardData,
            LocalDateTime chattedAt
    ) {
    }
}
