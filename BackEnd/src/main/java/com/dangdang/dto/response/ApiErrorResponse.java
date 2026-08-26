package com.dangdang.dto.response;

import java.time.LocalDateTime;

/**
 * 에러 발생 시 모든 API가 공통으로 반환하는 응답 형식입니다. (기획서 3.2 공통 API 규약)
 * 예) { "code": "AUTH_409_DUPLICATE_EMAIL", "message": "이미 존재하는 이메일입니다.", "timestamp": "..." }
 */
public record ApiErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp
) {
    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message, LocalDateTime.now());
    }
}
