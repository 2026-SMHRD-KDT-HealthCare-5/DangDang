package com.dangdang.repository;

import com.dangdang.entity.AiChat;
import com.dangdang.entity.ChatType;
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

    /**
     * [각주] (추가 2026-08-24) 챗봇 "일반 대화"에 멀티턴 맥락을 붙여주기 위한 조회입니다.
     * FastAPI(/rag/chat)는 매 요청을 완전히 독립적으로 처리해서(이전 대화를 기억 못 함),
     * Spring이 매번 최근 대화 몇 턴을 같이 실어 보내야 챗봇이 "방금 한 말"을 참고할 수 있습니다.
     * chatType=TEXT만 가져오는 이유 : NOTICE/FOOD_CARD 등은 "질문-답변" 형태의 대화 턴이
     * 아니라서 그대로 넣으면 오히려 맥락이 헷갈립니다. Top6(최근 3턴)면 충분하다고 판단했습니다 —
     * 늘리면 그만큼 매 채팅 요청마다 Gemini에 같이 실어 보내는 토큰도 늘어납니다.
     */
    List<AiChat> findTop6ByUserNoAndChatTypeOrderByChattedAtDesc(Integer userNo, ChatType chatType);
}
