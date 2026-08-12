package com.dangdang.repository;

import com.dangdang.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [각주 B] "레포지토리(Repository)"란?
 * DB에 대한 실제 접근(조회/저장/삭제)을 담당하는 계층입니다.
 * JpaRepository<User, Integer>를 상속만 하면, save()/findById()/delete() 같은 기본 기능은
 * 스프링이 자동으로 구현체를 만들어줍니다. (Integer는 User의 기본키 타입 = user_no)
 *
 * findByEmail 처럼 메서드 이름을 규칙에 맞게 지으면, "이름을 해석해서" SQL을 자동 생성해줍니다.
 * (Spring Data JPA의 "쿼리 메서드" 기능)
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
