# -*- coding: utf-8 -*-
"""
일반 대화(당당이 페르소나 응답 · 논문 기반 Q&A) 처리 서비스.

음식 인식/혈당 예측은 이 서비스의 역할이 아니다 — 그건 services/food_recognition.py가
전담한다. 여긴 순수 대화(페르소나 응답, 논문 기반 Q&A)만 처리한다.
"""

from core.config import client, MODEL_NAME, log_token_usage
from prompts.chat import (
    DOSAGE_ADJUSTMENT_STEMS,
    DRUG_BRAND_KEYWORDS,
    FIXED_KNOWLEDGE,
    INDIRECT_DRUG_REFERENCE,
    INSULIN_KEYWORDS,
    MEDICATION_DOSAGE_KEYWORDS,
    MEDICATION_SAFETY_MESSAGE,
    SYSTEM_PROMPT_TEMPLATE,
)
from services.rag.paper_qa import (
    answer_with_paper_cache,
    answer_without_cache,
    create_paper_cache,
    load_combined_text,
)

# ---------------------------------------------------------
# 사용자별 대화 세션 (멀티턴 메모리)
# 주의: 서버 메모리에만 저장되므로 서버 재시작하면 초기화됨.
#       추후 DB/Redis 등으로 영속화 필요.
# ---------------------------------------------------------
chat_sessions: dict = {}

# 사용자별 진단군 저장 (메모리, 서버 재시작 시 초기화 — 추후 DB로 영속화)
# 진단군을 아직 모르면 기본값 "건강군"으로 처리
user_diagnosis_groups: dict[str, str] = {}

# 모듈 최초 import 시 1회만 로드/생성되어 이후 재사용된다 (Python import 캐싱).
combined_papers_text = load_combined_text()
paper_cache = create_paper_cache(client, MODEL_NAME)


def get_or_create_chat_session(user_id: str, system_prompt: str):
    if user_id not in chat_sessions:
        chat_sessions[user_id] = client.chats.create(
            model=MODEL_NAME,
            config={
                "system_instruction": system_prompt,
                "temperature": 0.7,
            },
        )
    return chat_sessions[user_id]


def get_dummy_user_context(user_id: str) -> str:
    # 지금은 더미 데이터, 추후 DB 연동
    return f"""
[사용자 정보: {user_id}]
- 최근 식사: 흰쌀밥 + 제육볶음
- 오늘 걷기 기록: 아직 없음
- 최근 3일 평균 걷기: 18분
"""


def is_medication_dosage_question(message: str) -> bool:
    text = message.lower()  # 영문 브랜드명/단위 대소문자 무시하고 매칭

    if any(kw.lower() in text for kw in INSULIN_KEYWORDS):
        return True
    if any(kw.lower() in text for kw in DRUG_BRAND_KEYWORDS):
        return True
    if any(kw.lower() in text for kw in MEDICATION_DOSAGE_KEYWORDS):
        return True
    if any(kw in message for kw in INDIRECT_DRUG_REFERENCE):
        return True

    # "약"이라는 단어(또는 인슐린/브랜드명)와 조정 동사가 문장 안에 같이 있으면 차단
    # (반드시 붙어 있을 필요 없음: "약 좀 줄이고 싶은데" 같은 경우도 잡기 위함)
    has_drug_mention = (
        "약" in message
        or any(kw.lower() in text for kw in INSULIN_KEYWORDS)
        or any(kw.lower() in text for kw in DRUG_BRAND_KEYWORDS)
    )
    has_adjustment_verb = any(stem in message for stem in DOSAGE_ADJUSTMENT_STEMS)
    if has_drug_mention and has_adjustment_verb:
        return True

    return False


def answer_chat(user_id: str, message: str, diagnosis_group: str | None) -> str:
    """routers/chat.py의 POST /rag/chat 핸들러가 호출하는 진입점"""

    # 인슐린/약물 용량 관련 질문은 Gemini 호출 전에 키워드로 먼저 차단
    if is_medication_dosage_question(message):
        return MEDICATION_SAFETY_MESSAGE

    if diagnosis_group:
        user_diagnosis_groups[user_id] = diagnosis_group

    # 논문 기반 지식 질문 -> 논문 텍스트 컨텍스트로 답변
    if paper_cache:
        # 캐시가 있으면 논문을 매번 다시 안 보내고 캐시만 참조 (훨씬 빠름)
        response = answer_with_paper_cache(client, MODEL_NAME, message, paper_cache)
        log_token_usage(response, label="chat-paper-cached")
        return response.text

    if combined_papers_text:
        # 캐시 생성이 실패했을 때의 폴백 (느리지만 동작은 함)
        response = answer_without_cache(client, MODEL_NAME, message, combined_papers_text)
        log_token_usage(response, label="chat-paper-nocache")
        return response.text

    # 논문 리소스가 아예 없을 때의 최종 폴백 -> 페르소나 대화 세션
    user_context = get_dummy_user_context(user_id)
    system_prompt = SYSTEM_PROMPT_TEMPLATE.format(
        knowledge=FIXED_KNOWLEDGE,
        user_context=user_context,
    )
    chat_session = get_or_create_chat_session(user_id, system_prompt)
    response = chat_session.send_message(message)
    log_token_usage(response, label="chat")
    return response.text
