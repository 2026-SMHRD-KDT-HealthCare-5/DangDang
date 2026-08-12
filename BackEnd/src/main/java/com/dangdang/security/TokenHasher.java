package com.dangdang.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * [각주 N] refreshToken 문자열을 DB에 저장하기 전에 SHA-256으로 해시(요약값 변환)합니다.
 *
 * 비밀번호는 BCrypt(느리고 매번 다른 결과)를 쓰지만, 토큰은 SHA-256(빠르고 항상 같은 결과)을 씁니다.
 * 이유: 비밀번호는 사람이 외워서 재사용하므로 "무차별 대입 공격을 일부러 느리게" 만들어야 하지만,
 * refreshToken은 이미 JWT 라이브러리가 만든 무작위에 가까운 긴 문자열이라 애초에 추측이 불가능합니다.
 * 게다가 로그인 상태 확인마다 "DB에 저장된 해시 = 지금 들어온 토큰을 해시한 값" 을 바로 비교(=)해야
 * 하므로, 매번 다른 결과가 나오는 BCrypt로는 이 비교 자체가 불가능합니다.
 */
public final class TokenHasher {

    private TokenHasher() {
    }

    public static String sha256(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // 자바 표준 런타임에는 SHA-256이 항상 포함되어 있어 실제로는 발생하지 않는 예외입니다.
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
