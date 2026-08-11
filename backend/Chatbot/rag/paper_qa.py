# -*- coding: utf-8 -*-
"""
논문 기반 답변 모듈 (텍스트 통합본 + 컨텍스트 캐시, RAG/PDF업로드 없음)

extract_text.py로 미리 만들어둔 papers_combined.txt(논문 7개를 텍스트로
추출해서 헤더로 구분해 하나로 합친 파일)를 컨텍스트 캐시에 등록해두고,
일반 건강 질문이 오면 캐시를 참조해서 빠르게 답한다.

PDF를 그대로 첨부하던 이전 방식보다 빠른 이유:
- Gemini는 PDF를 페이지 이미지처럼 처리(비전 모델 경유)하는데,
  순수 텍스트는 이 과정이 없어 처리 속도/토큰 비용이 크게 줄어듦
- 파일 7개 대신 텍스트 1개만 다루면 되어 구조도 단순해짐
"""

from pathlib import Path

SCRIPT_DIR = Path(__file__).parent
COMBINED_TEXT_PATH = SCRIPT_DIR / "papers_combined.txt"
CACHE_NAME_PATH = SCRIPT_DIR / ".paper_cache_name"  # 재시작 시 캐시 재사용을 위한 기록 파일

# 캐시 유지 기간. TTL은 상한이 없어서 원하는 만큼 길게 잡을 수 있지만,
# 저장해두는 동안 비용이 계속 나가니 "무한대"보다는 프로젝트 기간에 맞춰 설정하는 게 합리적임.
# 발표 전까지 계속 켜둘 거면 이 값을 늘리면 됨 (예: 30일 = "2592000s")
CACHE_TTL = "2592000s"  # 30일

PAPER_QA_INSTRUCTION = (
    "아래는 여러 학술논문/정부보고서를 텍스트로 추출해서 합쳐놓은 자료야. "
    "각 논문은 '[논문 제목] ... [저자] ... [발행연도] ...' 헤더로 구분되어 있어. "
    "사용자 질문에 답할 때 이 자료들의 내용을 근거로 답하되, "
    "원문을 그대로 베끼지 말고 네 말투로 자연스럽게 풀어서 설명해줘. "
    "의학적 확진처럼 단정하지 말고, 필요하면 전문의 상담을 권유해. "
    "답변 끝에는 참고한 논문 제목을 간단히 한 줄로 밝혀줘."
)


def load_combined_text() -> str:
    """
    papers_combined.txt를 읽어옴. 없으면 extract_text.py를 자동으로 돌려서 생성.
    (최초 1회는 PDF 추출 때문에 시간이 좀 걸릴 수 있음. 그다음부터는 파일 재사용)
    """
    if not COMBINED_TEXT_PATH.exists():
        print("[paper_qa] papers_combined.txt 없음 -> 자동 생성 시도")
        try:
            try:
                from .extract_text import build_combined_text
            except ImportError:
                from extract_text import build_combined_text
            combined = build_combined_text()
            COMBINED_TEXT_PATH.write_text(combined, encoding="utf-8")
            print(f"[paper_qa] 생성 완료: {COMBINED_TEXT_PATH}")
        except Exception as e:
            print(f"[paper_qa] 자동 생성 실패: {e}")
            return ""

    return COMBINED_TEXT_PATH.read_text(encoding="utf-8")


def _try_reuse_existing_cache(client):
    """
    이전 실행에서 만들어둔 캐시가 아직 살아있으면(TTL 안 지났으면) 그걸 재사용.
    재사용할 때 TTL도 같이 연장해서, 서버를 계속 껐다 켜는 동안은
    사실상 만료 걱정 없이 계속 쓸 수 있게 함.
    """
    from google.genai import types

    if not CACHE_NAME_PATH.exists():
        return None

    cache_name = CACHE_NAME_PATH.read_text(encoding="utf-8").strip()
    if not cache_name:
        return None

    try:
        cache = client.caches.get(name=cache_name)
        cache = client.caches.update(
            name=cache_name,
            config=types.UpdateCachedContentConfig(ttl=CACHE_TTL),
        )
        print(f"[paper_qa] 기존 캐시 재사용 + TTL 연장: {cache.name} (새로 안 만듦, 토큰 절약)")
        return cache
    except Exception:
        # 캐시가 만료됐거나 삭제된 경우 -> 새로 만들어야 함
        print("[paper_qa] 기존 캐시가 만료/삭제됨 -> 새로 생성")
        return None


def create_paper_cache(client, model_name: str):
    """
    합쳐진 논문 텍스트를 Gemini 컨텍스트 캐시로 등록.
    이전에 만들어둔 캐시가 아직 유효하면 재사용하고, 없으면 새로 만듦.
    반환: cache 객체 (실패하면 None -> 호출부에서 캐시 없이 폴백)
    """
    from google.genai import types

    existing = _try_reuse_existing_cache(client)
    if existing:
        return existing

    combined_text = load_combined_text()
    if not combined_text:
        print("[paper_qa] 논문 텍스트가 비어있어 캐시를 만들지 않음")
        return None

    try:
        cache = client.caches.create(
            model=model_name,
            config=types.CreateCachedContentConfig(
                display_name="dangdangi_papers_text_cache",
                contents=[combined_text],
                system_instruction=PAPER_QA_INSTRUCTION,
                ttl=CACHE_TTL,
            ),
        )
        print(f"[paper_qa] 컨텍스트 캐시 새로 생성 완료: {cache.name} (원문 {len(combined_text):,}자)")
        CACHE_NAME_PATH.write_text(cache.name, encoding="utf-8")
        return cache
    except Exception as e:
        print(f"[paper_qa] 캐시 생성 실패, 매번 텍스트 첨부하는 방식으로 폴백: {e}")
        return None


def answer_with_paper_cache(client, model_name: str, question: str, cache):
    """캐시된 논문 텍스트를 이용해 빠르게 답변 생성"""
    from google.genai import types

    response = client.models.generate_content(
        model=model_name,
        contents=question,
        config=types.GenerateContentConfig(cached_content=cache.name),
    )
    return response


def answer_without_cache(client, model_name: str, question: str, combined_text: str):
    """
    캐시를 못 쓸 때의 폴백. 매번 논문 텍스트 전체를 프롬프트에 직접 넣어서 호출.
    (캐시 방식보다 느리고 비용이 더 들지만, 캐시 생성이 실패해도 서비스는 계속 되게 함)
    """
    prompt = f"{PAPER_QA_INSTRUCTION}\n\n{combined_text}\n\n사용자 질문: {question}"
    response = client.models.generate_content(model=model_name, contents=prompt)
    return response