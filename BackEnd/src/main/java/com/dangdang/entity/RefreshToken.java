package com.dangdang.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * [각주 M] refresh_token 테이블과 매핑되는 엔티티입니다.
 * User 엔티티가 "회원 정보"를 담당한다면, 이 엔티티는 "이 회원에게 지금 유효한 refreshToken이
 * 무엇인지"를 서버가 기억하기 위한 기록입니다. (원본 토큰이 아니라 해시값만 저장 — TokenHasher 참고)
 *
 * 이 엔티티가 생기기 전에는(무상태 인증만 썼을 때) 다음 문제들이 있었습니다.
 * 1) 로그아웃을 해도 서버가 그 토큰을 "무효"로 만들 방법이 없어, 탈취되면 만료 전까지 계속 쓰임
 * 2) 비밀번호를 바꾸거나 계정에 이상이 생겨도 이미 발급된 토큰들을 강제로 끊을 수 없음
 * 3) 토큰이 도난당해 재사용되는 정황(같은 refreshToken이 두 번 쓰이는 등)을 서버가 알아챌 수 없음
 * 이 테이블 + AuthService의 회전(rotation) 로직이 세 가지를 모두 해결합니다.
 */
@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_no")
    private Long tokenNo;

    @Column(name = "user_no", nullable = false)
    private Integer userNo;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Builder
    private RefreshToken(Integer userNo, String tokenHash, LocalDateTime expiresAt) {
        this.userNo = userNo;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
    }

    /** [각주] 상태 전이 메서드: "이 토큰을 지금부터 못 쓰게 만든다"는 의미를 이름으로 드러냅니다. */
    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
