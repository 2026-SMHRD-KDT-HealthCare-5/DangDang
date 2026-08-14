# -*- coding: utf-8 -*-
"""
논문 기반 답변 모듈 (요약 텍스트 직접 첨부 방식, 캐시/RAG/PDF업로드 없음)

사람이 미리 추려서 만들어둔 papers_combined.txt(논문 7편에서 서비스에
실제로 쓸 내용만 골라 정리한 요약본, ~6,446토큰)를 매번 질문과 함께
프롬프트에 그대로 넣어서 답한다.

※ 2026-08-13 이전에는 논문 원문 전체(202,938자 ≈ 125,875토큰)를 Gemini
컨텍스트 캐시에 올려두고 재사용하는 방식이었는데, 요약본으로 지식
소스를 교체하면서 컨텍스트 자체가 작아져 캐시 저장비용/무효화 관리
부담을 감수할 이유가 없어졌다 (게다가 캐시 삭제를 로컬 기록 파일만
지우고 서버 쪽 캐시 객체는 안 지워서 저장비용이 새는 사고도 있었음).
그래서 캐싱 관련 코드는 전부 제거하고 answer_without_cache() 방식만
남겼다. papers_combined.txt는 여전히 pdf가 아니라 텍스트라서, PDF를
비전 모델로 처리하던 방식보다 처리 속도/토큰 비용이 낮다.

목차
1. 상수 — COMBINED_TEXT_PATH, PAPER_QA_INSTRUCTION(답변 지침 프롬프트)
2. load_combined_text() — papers_combined.txt를 읽어옴
3. answer_without_cache() — 매번 논문 요약 텍스트 전체를 프롬프트에 직접 넣어 답변 생성
"""

from pathlib import Path

from prompts.persona import (
    DANGDANGI_IDENTITY,
    DANGDANGI_SAFETY_RULES,
    DANGDANGI_SCOPE_RULES,
    DANGDANGI_SOURCE_ATTRIBUTION_RULE,
    DANGDANGI_TONE_RULES,
)

SCRIPT_DIR = Path(__file__).parent
COMBINED_TEXT_PATH = SCRIPT_DIR / "papers_combined.txt"

PAPER_QA_INSTRUCTION = (
    DANGDANGI_IDENTITY + "\n\n" +
    DANGDANGI_TONE_RULES + "\n\n" +
    DANGDANGI_SCOPE_RULES + "\n\n"
    "[참고 자료 안내]\n"
    "아래는 여러 학술논문/정부보고서에서 이 서비스(음식 사진 기반 혈당예측 + 식후 "
    "걷기미션)에 실제로 쓸 수 있는 내용만 추린 요약 자료야. 각 섹션은 "
    "'[출처: ...]' 형식으로 원 논문을 표시해뒀어. 질문이 이 자료와 관련 있을 때만 "
    "내용을 근거로 답하되, 원문을 그대로 베끼지 말고 네 말투로 자연스럽게 풀어서 "
    "설명해줘. " + DANGDANGI_SOURCE_ATTRIBUTION_RULE + "\n\n"
    "[답변 원칙]\n" +
    DANGDANGI_SAFETY_RULES + "\n"
    "- 실제로 위 자료 내용을 근거로 답했을 때만, 답변 끝에 참고한 논문 제목을 "
    "간단히 한 줄로 밝혀줘 (질문이 자료와 무관해서 안 썼으면 생략)"
)


def load_combined_text() -> str:
    """
    papers_combined.txt(사람이 미리 추린 논문 요약본)를 읽어옴.

    2026-08-14: 예전엔 이 파일이 없으면 extract_text.py로 원문 PDF를 자동
    추출해서 만드는 폴백이 있었는데, 요약본 방식으로 완전히 굳히면서
    원문 추출 체인(extract_text.py/sources.py/papers/ocr_cache) 자체를
    지웠다. 이제 이 파일이 없으면 그냥 빈 문자열을 반환하고, 호출부
    (services/rag_chat.py)가 페르소나 전용 폴백으로 넘어간다.
    """
    if not COMBINED_TEXT_PATH.exists():
        print(f"[paper_qa] {COMBINED_TEXT_PATH} 없음 — 논문 요약본을 팀원에게 받아서 이 경로에 두세요")
        return ""

    return COMBINED_TEXT_PATH.read_text(encoding="utf-8")


def answer_without_cache(client, model_name: str, question: str, combined_text: str):
    """
    매번 논문 요약 텍스트 전체를 프롬프트에 직접 넣어서 호출 (지금 유일하게 쓰는 경로).
    요약본이라 컨텍스트가 작아서(~6,446토큰) 캐시 없이도 비용 부담이 크지 않음.
    """
    prompt = f"{PAPER_QA_INSTRUCTION}\n\n{combined_text}\n\n사용자 질문: {question}"
    response = client.models.generate_content(model=model_name, contents=prompt)
    return response