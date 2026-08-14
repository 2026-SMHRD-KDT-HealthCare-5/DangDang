package com.dangdang.repository;

import com.dangdang.entity.AiChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [각주 AB] ai_chat 테이블 접근 담당.
 * findByUserNoAndChattedAtBetween 처럼 메서드 이름을 규칙대로 지으면 스프링이
 * "WHERE user_no = ? AND chatted_at BETWEEN ? AND ?" 같은 SQL을 알아서 만들어줍니다.
 * (UserRepository의 [각주 B] 참고 — 여기선 idx_ai_chat_user_at(user_no, chatted_at) 인덱스를 그대로 탑니다)
 */
public interface AiChatRepository extends JpaRepository<AiChat, Integer> {

    /**
     * [각주 AC] 노션 명세의 "반개구간 [date 00:00:00, date+1일 00:00:00)"을 그대로 구현합니다.
     * 그래서 끝 경계는 <=가 아니라 <(Before)를 씁니다 — 자정 정각에 생긴 다음날 기록이
     * 실수로 오늘 이력에 섞이지 않도록 하기 위함입니다.
     */
    List<AiChat> findByUserNoAndChattedAtGreaterThanEqualAndChattedAtLessThanOrderByChattedAtAsc(
            Integer userNo, LocalDateTime start, LocalDateTime endExclusive);
}
