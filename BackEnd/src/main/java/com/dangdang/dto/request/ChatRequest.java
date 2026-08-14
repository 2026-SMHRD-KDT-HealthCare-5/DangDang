package com.dangdang.dto.request;

/**
 * [각주 AD] POST /api/chat 요청 바디. 노션 명세 기준 body는 message 하나뿐입니다.
 * userNo는 항상 JWT에서 꺼내 쓰므로(다른 컨트롤러와 동일한 원칙) 여기엔 없습니다.
 */
public record ChatRequest(
        String message
) {
}
