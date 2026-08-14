package com.dangdang.entity;

/**
 * [각주 Z] 챗봇 화면에 그려지는 말풍선·카드의 "종류"입니다.
 * ai_chat.chat_type 컬럼(varchar(20))에 이 enum의 이름이 문자열 그대로 저장됩니다.
 * (예: ChatType.TEXT → DB에는 "TEXT"로 저장됨 — @Enumerated(EnumType.STRING) 덕분)
 *
 * 프론트는 이 값 하나로 "이 레코드를 무슨 모양으로 그릴지" 정합니다 — 노션 "백엔드 가이드" 6.4절 기준.
 * 카드에 필요한 수치(및 원본 참조 PK)는 전부 AiChat.cardData(JSON 스냅샷) 안에 함께 저장됩니다
 * (예: FOOD_CARD면 cardData 안에 logNo도 같이 넣는 식 — 별도 ref_no 컬럼은 안 씁니다).
 */
public enum ChatType {
    TEXT,          // 일반 텍스트 말풍선 (사용자 질문 + AI 답변) — cardData 없음
    NOTICE,        // 시스템 안내 · 선제 알림 — cardData 없음
    PRE_GLUCOSE,   // 식전 혈당 입력 카드 — cardData 없음
    FOOD_CARD,     // 음식 확인 카드 — cardData에 logNo + 영양정보 등 스냅샷
    MISSION_CARD,  // 추천 걷기 미션 카드 — cardData에 missionNo + 목표치 스냅샷
    POST_GLUCOSE,  // 걷기 후 혈당 입력 카드 — cardData에 missionNo 스냅샷
    RESULT_CARD,   // 걷기 결과 요약 카드 — cardData에 missionNo + 결과 스냅샷
    CHEER          // 응원 메시지 — cardData에 missionNo 스냅샷
}
