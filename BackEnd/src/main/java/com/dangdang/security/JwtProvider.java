package com.dangdang.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * [각주 F] JWT(JSON Web Token)란?
 * 로그인에 성공하면 서버가 발급해주는 "디지털 출입증"입니다.
 * 이 안에는 "누구인지(userNo)"와 "언제까지 유효한지(만료시간)" 같은 정보가 암호학적 서명과 함께
 * 담겨 있어서, 서버가 매 요청마다 DB에서 세션을 조회하지 않고도 토큰만 검증하면 "이 사람이 맞다"고
 * 확인할 수 있습니다. (이런 방식을 "무상태(stateless) 인증"이라 합니다)
 *
 * - accessToken : 짧게 사는 토큰. 실제 API 호출마다 Authorization 헤더에 실어보냅니다.
 * - refreshToken : 길게 사는 토큰. accessToken이 만료되면 재발급받는 용도로만 사용합니다.
 *
 * 이 클래스는 토큰을 "만들고(create)" "검증하고(validate)" "내용을 꺼내는(get)" 역할을 합니다.
 */
@Component
public class JwtProvider {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs
    ) {
        // [각주] HMAC-SHA256 서명에 쓸 비밀키를 문자열로부터 만듭니다.
        // 이 키를 아는 사람만 "진짜" 토큰을 만들 수 있고, 위조된 토큰은 검증 단계에서 걸러집니다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String createAccessToken(Integer userNo) {
        return createToken(userNo, accessTokenExpirationMs, TYPE_ACCESS);
    }

    public String createRefreshToken(Integer userNo) {
        return createToken(userNo, refreshTokenExpirationMs, TYPE_REFRESH);
    }

    private String createToken(Integer userNo, long expirationMs, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userNo)) // 토큰의 주인(사용자 식별자)
                .claim(CLAIM_TYPE, type)          // access인지 refresh인지 구분하는 값
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /** 서명이 올바르고 만료되지 않았으면 true. (위조/만료 토큰은 예외가 발생하므로 false 처리) */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Integer getUserNo(String token) {
        String subject = parseClaims(token).getSubject();
        return Integer.valueOf(subject);
    }

    public boolean isRefreshToken(String token) {
        String type = parseClaims(token).get(CLAIM_TYPE, String.class);
        return TYPE_REFRESH.equals(type);
    }

    public boolean isAccessToken(String token) {
        String type = parseClaims(token).get(CLAIM_TYPE, String.class);
        return TYPE_ACCESS.equals(type);
    }

    /**
     * [각주 O] 토큰 안에 이미 박혀 있는 만료시간(exp claim)을 그대로 꺼내옵니다.
     * refresh_token 테이블의 expires_at 컬럼에 저장할 때, "만료시간 ms를 여기서도 또 계산"하지 않고
     * 방금 만든 토큰 문자열에서 바로 읽어와 두 값이 어긋날 일이 없게 합니다.
     */
    public LocalDateTime getExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
