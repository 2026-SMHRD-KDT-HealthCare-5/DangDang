package com.dangdang.dto.response;

/**
 * 로그인 성공 응답 (200 OK) — 명세: "성공: 200 OK + accessToken, refreshToken"
 * - accessToken : 짧은 유효기간(기본 1시간). 이후 모든 API 요청에 Authorization 헤더로 실어보냅니다.
 * - refreshToken : 긴 유효기간(기본 14일). accessToken이 만료되면 이 토큰으로 재발급 받습니다.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
