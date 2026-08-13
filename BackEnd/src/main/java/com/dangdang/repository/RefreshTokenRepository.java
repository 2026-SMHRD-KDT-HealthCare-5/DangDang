package com.dangdang.repository;

import com.dangdang.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * [각주 B-1] UserRepository와 똑같은 원리입니다 — JpaRepository를 상속하는 것만으로
 * save()/findById() 같은 기본 기능은 이미 다 갖춰져 있고, 여기서는 우리에게 필요한
 * "조회/일괄 폐기" 전용 메서드만 추가로 선언합니다.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 해시값으로 토큰 레코드를 찾습니다. revoked 여부와 상관없이 찾아야 "이미 폐기된 토큰 재사용"도
    // 구분해낼 수 있습니다 (AuthService.refresh() 참고).
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * [각주] @Modifying + @Query : Spring Data JPA가 이름만으로 만들어주는 조회용 메서드와 달리,
     * "여러 행을 한 번에 UPDATE" 하는 것은 JPQL(자바 객체 기준 쿼리 언어)을 직접 적어줘야 합니다.
     * 로그아웃 시 이 사용자의 살아있는(revoked=false) 토큰을 전부 한 번에 폐기 처리합니다.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE rt.userNo = :userNo AND rt.revoked = false")
    int revokeAllByUserNo(@Param("userNo") Integer userNo);
}
