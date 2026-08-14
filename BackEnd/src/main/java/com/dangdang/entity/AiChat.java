package com.dangdang.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * [각주 AA] ai_chat 테이블과 매핑되는 엔티티입니다 — 당당이 챗봇 화면에 보이는
 * 말풍선·카드 전부가 이 테이블 1레코드씩으로 저장됩니다.
 *
 * user_message는 nullable이지만 ai_message는 실제 DB에서 NOT NULL입니다 — 모든 레코드는
 * "당당이가 뭐라고 답했는지/보여줬는지"는 항상 있어야 한다는 뜻입니다. 사용자가 아무 말도
 * 안 하고 서버가 카드만 띄운 경우(FOOD_CARD 등)엔 userMessage만 null이 됩니다.
 *
 * cardData는 JSON을 그대로 문자열로 저장해둡니다(컬럼 타입은 DB에서 jsonb/text 무엇이든 무방).
 * 프론트가 과거 이력을 다시 그릴 때 원본 테이블(intake_log/walk_mission)을 재조회하지 않고
 * 이 스냅샷만으로 그리기 위함입니다(원본이 나중에 바뀌어도 과거 카드 내용은 그대로 보존됨).
 *
 * [각주 AK] (수정) 원래는 참조 PK를 담는 ref_no 컬럼을 따로 뒀는데, 어차피 카드 내용 전체를
 * cardData에 JSON으로 스냅샷 저장하기로 했으니 그 안에 logNo/missionNo 같은 참조값도 같이
 * 넣기로 결정이 바뀌었습니다. 그래서 ref_no 컬럼/필드는 더 이상 안 씁니다.
 */
@Entity
@Table(name = "ai_chat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_chat_no")
    private Integer aiChatNo;

    @Column(name = "user_no", nullable = false)
    private Integer userNo;

    @Column(name = "user_message", length = 500)
    private String userMessage;

    // [각주] 실제 DB 스키마(DangDang_schema.md) 기준 ai_message는 NOT NULL, 최대 1000자입니다.
    // 답변이 이보다 길면 INSERT 자체가 실패하므로, ChatService에서 저장 전에 잘라내는 처리가 필요합니다.
    @Column(name = "ai_message", nullable = false, length = 1000)
    private String aiMessage;

    // [각주] @Enumerated(EnumType.STRING) : enum을 숫자(ORDINAL, 순서값)가 아니라
    // 이름 그대로("TEXT" 등) 문자열로 저장하게 하는 설정입니다. 숫자로 저장하면 enum 순서가
    // 바뀔 때 과거 데이터가 전부 엉뚱한 값으로 읽히는 사고가 나서, 항상 STRING을 씁니다.
    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type", nullable = false, length = 20)
    private ChatType chatType;

    @Column(name = "card_data", columnDefinition = "text")
    private String cardData;

    @Column(name = "chatted_at", nullable = false, updatable = false)
    private LocalDateTime chattedAt;

    @Builder
    private AiChat(Integer userNo, String userMessage, String aiMessage,
                   ChatType chatType, String cardData) {
        this.userNo = userNo;
        this.userMessage = userMessage;
        this.aiMessage = aiMessage;
        this.chatType = chatType;
        this.cardData = cardData;
    }

    @PrePersist
    protected void onCreate() {
        this.chattedAt = LocalDateTime.now();
    }
}
