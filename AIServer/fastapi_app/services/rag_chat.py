# -*- coding: utf-8 -*-
"""
일반 대화(당당이 페르소나 응답 · 논문 기반 Q&A) 처리 서비스.

음식 인식/혈당 예측은 이 서비스의 역할이 아니다 — 그건 services/food_recognition.py가
전담한다. 여긴 순수 대화(페르소나 응답, 논문 기반 Q&A)만 처리한다.

※ 2026-08-19: 예전엔 curated_knowledge_text(요약본)만 있으면 모든 메시지가 무조건
paper_qa로 흘러갔다. "안녕!"같은 잡담도 논문 근거자료 + 인용지침이 붙은 프롬프트로
처리되다보니 팀원 피드백("말이 너무 길다", "무조건 논문 출처를 씀", "일상 대화가
안 됨")이 들어와서, 메시지를 먼저 가볍게 분류(is_diet_health_related)해 당뇨/혈당/
식사/운동 관련 질문일 때만 paper_qa를 타고, 그 외엔 캐주얼 페르소나 경로로 보낸다.

목차
1. chat_sessions, user_diagnosis_groups — 사용자별(user_no 기준) 대화/진단군을 메모리에 저장 (서버 재시작 시 초기화)
2. curated_knowledge_text — 모듈이 처음 로딩될 때 1회만 읽어두는 논문 요약본
3. get_or_create_chat_session() — 사용자 ID별로 대화 세션을 만들거나 가져옴
4. get_dummy_user_context() — 지금은 더미인 사용자 정보 (추후 DB 연동 예정)
5. is_medication_dosage_question() — 약물 용량 질문인지 키워드로 판별
6. is_diet_health_related() — 당뇨/혈당/식사/운동 관련 질문인지 가볍게 분류 (RAG 라우팅용)
7. answer_chat() — routers/chat.py가 호출하는 실제 진입점, 답변 생성의 전체 흐름
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
from services.rag.paper_qa import answer_without_cache, load_combined_text

# ---------------------------------------------------------
# 2026-08-13: 논문 지식 소스를 원본 7편 전체(202,938자 ≈ 125,875토큰)에서
# 서비스 목적(음식 사진 기반 혈당예측 + 식후 걷기미션)에 맞게 미리 추린
# 요약본(10,398자 ≈ 6,446토큰)으로 교체함. papers_combined.txt 파일
# 자체를 이 요약본으로 덮어썼음 — load_combined_text()는 코드 안 바꿔도 됨.
#
# 캐시(Gemini context cache)는 이제 안 쓴다 — 컨텍스트가 원래도 작아서
# (6,446토큰) 캐시 저장비용/무효화 관리 부담을 감수할 만큼의 이득이 없음
# (어제 캐시 관리 실수로 저장비용이 새던 사고도 있었고). 매번 직접
# 전송하는 answer_without_cache()만 쓴다.
# ---------------------------------------------------------

# ---------------------------------------------------------
# 사용자별 대화 세션 (멀티턴 메모리)
# 주의: 서버 메모리에만 저장되므로 서버 재시작하면 초기화됨.
#       추후 DB/Redis 등으로 영속화 필요.
# ---------------------------------------------------------
chat_sessions: dict = {}

# 사용자별 진단군 저장 (메모리, 서버 재시작 시 초기화 — 추후 DB로 영속화)
# 진단군을 아직 모르면 기본값 "건강군"으로 처리
# 키는 user_no(정수, DB의 USER 테이블 PK와 동일 타입)
user_diagnosis_groups: dict[int, str] = {}

# 모듈 최초 import 시 1회만 로드되어 이후 재사용된다 (Python import 캐싱).
curated_knowledge_text = load_combined_text()


def get_or_create_chat_session(user_no: int, system_prompt: str):
    if user_no not in chat_sessions:
        chat_sessions[user_no] = client.chats.create(
            model=MODEL_NAME,
            config={
                "system_instruction": system_prompt,
                "temperature": 0.7,
            },
        )
    return chat_sessions[user_no]


def get_dummy_user_context(user_no: int) -> str:
    # 지금은 더미 데이터, 추후 DB 연동
    return f"""
[사용자 정보: {user_no}]
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


# 당뇨/혈당/식사/운동과 무관해 보이는데도 분류기를 매번 태우면 그만큼 지연/비용이
# 붙으니, 아주 명백한 잡담(인사말 등)은 분류 호출 자체를 생략하는 게 낫다. 다만
# 이 리스트는 "확실히 캐주얼"만 걸러내는 용도라 넓게 잡지 않는다 — 애매하면
# 분류기를 태우는 쪽(= RAG 오탐 방지보다 캐주얼 오탐 방지를 우선)이 안전하다.
OBVIOUS_GREETING_KEYWORDS = ["안녕", "hi", "hello", "고마워", "감사", "잘가", "바이"]


def is_diet_health_related(message: str) -> bool:
    """
    당뇨/혈당/식사/운동/건강관리와 관련된 질문인지 가볍게 판별 (RAG 라우팅용).

    "떡볶이 먹어도 돼?"처럼 "당뇨"/"혈당" 같은 키워드가 전혀 없는 음식 관련 질문이
    이 서비스의 핵심 사용 시나리오라 키워드 매칭으로는 못 잡는다 (음식명이 사실상
    무한함). 그래서 flash-lite로 초경량 YES/NO 분류 호출을 한 번 더 한다 — 프롬프트가
    짧아서 토큰/지연 부담은 미미하다.
    """
    text = message.strip().lower()
    if any(kw in text for kw in OBVIOUS_GREETING_KEYWORDS) and len(text) <= 10:
        return False

    response = client.models.generate_content(
        model=MODEL_NAME,
        contents=message,
        config={
            "system_instruction": (
                "사용자 메시지가 당뇨병·혈당·식사(특정 음식 포함)·운동·건강관리와 "
                "관련 있는 질문인지 판단해. 관련 있으면 'YES', 인사/잡담/완전히 "
                "무관한 주제면 'NO'만 출력해. 다른 말은 절대 하지 마."
            ),
            "temperature": 0.0,
        },
    )
    log_token_usage(response, label="chat-route")
    return response.text.strip().upper().startswith("YES")


def _format_history(history: list | None) -> str:
    """
    [각주] (추가 2026-08-24, 버그 9) Spring이 ai_chat에서 조회해 보내준 최근 대화 턴을
    "사용자: .../ 당당이: ..." 형식의 텍스트로 풀어서, answer_without_cache()의 프롬프트에
    그대로 끼워 넣을 수 있게 만듭니다. history가 비어있으면(첫 대화 등) 빈 문자열을 반환합니다.
    """
    if not history:
        return ""

    lines = []
    for turn in history:
        user_message = getattr(turn, "user_message", None)
        ai_message = getattr(turn, "ai_message", None)
        if user_message:
            lines.append(f"사용자: {user_message}")
        if ai_message:
            lines.append(f"당당이: {ai_message}")
    return "\n".join(lines)


def answer_chat(user_no: int, message: str, diagnosis_group: str | None,
                 history: list | None = None) -> str:
    """routers/chat.py의 POST /rag/chat 핸들러가 호출하는 진입점

    [각주] (수정 2026-08-24, 버그 9) history 파라미터를 추가했습니다 — 이전엔 매 요청을
    완전히 독립적으로 처리해서 직전 대화를 전혀 기억 못 했습니다(대화가 뚝뚝 끊기는
    느낌의 원인). Spring이 최근 대화 몇 턴을 실어 보내면 그걸 프롬프트에 같이 넣어줍니다.
    """

    # 인슐린/약물 용량 관련 질문은 Gemini 호출 전에 키워드로 먼저 차단
    if is_medication_dosage_question(message):
        return MEDICATION_SAFETY_MESSAGE

    if diagnosis_group:
        user_diagnosis_groups[user_no] = diagnosis_group

    # 당뇨/혈당/식사/운동 관련 질문만 논문 요약 지식 기반(RAG)으로 답변
    if curated_knowledge_text and is_diet_health_related(message):
        response = answer_without_cache(client, MODEL_NAME, message, curated_knowledge_text)
        log_token_usage(response, label="chat-paper")
        return response.text

    # 그 외(잡담 등)는 논문 자료 없이 가벼운 페르소나 대화 세션으로 응답
    # (요약본 파일이 아예 없을 때도 여기로 온다 — 최종 폴백 겸 캐주얼 경로)
    user_context = get_dummy_user_context(user_no)
    system_prompt = SYSTEM_PROMPT_TEMPLATE.format(
        knowledge=FIXED_KNOWLEDGE,
        user_context=user_context,
    )
    chat_session = get_or_create_chat_session(user_no, system_prompt)
    response = chat_session.send_message(message)
    log_token_usage(response, label="chat-casual")
    return response.text
