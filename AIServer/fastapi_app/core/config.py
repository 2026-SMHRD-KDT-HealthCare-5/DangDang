# -*- coding: utf-8 -*-
"""
여러 파일이 공통으로 갖다 쓰는 "재료 창고" 역할을 하는 파일.
환경변수 로드, Gemini 클라이언트 생성, 여러 서비스가 같이 쓰는 상수/함수를 모아둔다.

목차
1. APP_ROOT, load_dotenv() — fastapi_app/.env 파일을 읽어서 환경변수로 등록
2. GEMINI_API_KEY, client — Gemini API 접속 정보와 클라이언트 객체
3. MODEL_NAME — 사용할 Gemini 모델 이름
4. DIAGNOSIS_GROUPS — 진단군 목록 ("건강군" / "전당뇨" / "2형당뇨")
5. PRE_GLUCOSE_DEFAULTS, get_pre_glucose_default() — 식전 혈당을 안 넣었을 때 쓸 기본값
6. log_token_usage() — Gemini 호출 후 토큰 사용량을 터미널에 찍어주는 함수
7. db_engine — PostgreSQL 연결 엔진 (읽기 전용 조회 전용. INSERT/UPDATE/DELETE는 Spring 담당)

core/는 다른 계층(services, repositories, routers)이 참조하는 최하위 계층이다.
core가 services나 routers를 import하는 일은 없어야 한다 (역방향 의존 금지).
"""

import os
from pathlib import Path

from dotenv import load_dotenv
from google import genai
from sqlalchemy import create_engine

# fastapi_app/ 루트 기준으로 .env를 찾는다.
# uvicorn을 다른 위치(예: AIServer/)에서 실행해도 항상 같은 파일을 읽도록
# __file__ 기준 상대경로로 고정한다 (os.getcwd() 기준이면 실행 위치에 따라 깨짐).
APP_ROOT = Path(__file__).resolve().parent.parent
load_dotenv(APP_ROOT / ".env")

GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY")
client = genai.Client(api_key=GEMINI_API_KEY)

# PostgreSQL 연결 엔진. FastAPI는 조회(SELECT)만 하고, 쓰기(INSERT/UPDATE/DELETE)는
# 전부 Spring이 담당한다 (노션 "백엔드 가이드" 아키텍처 원칙).
# create_engine()은 여기서 바로 접속하는 게 아니라 "연결 방법"만 준비해두는 것이고,
# 실제 접속은 나중에 쿼리를 실행하는 시점에 커넥션 풀에서 하나 꺼내 쓴다.
DB_URL = os.environ.get("DB_URL")
DB_PORT = os.environ.get("DB_PORT", "5432")
DB_NAME = os.environ.get("DB_NAME")
DB_USERNAME = os.environ.get("DB_USERNAME")
DB_PASSWORD = os.environ.get("DB_PASSWORD")

db_engine = None
if DB_URL and DB_NAME and DB_USERNAME:
    db_engine = create_engine(
        f"postgresql+psycopg2://{DB_USERNAME}:{DB_PASSWORD}@{DB_URL}:{DB_PORT}/{DB_NAME}"
    )

# gemini-2.5-flash-lite는 신규 사용자에게 더 이상 제공되지 않음.
# gemini-3.1-flash-lite로 대체 (저렴하면서 최신 모델)
MODEL_NAME = "gemini-3.1-flash-lite"

# 진단군 — glucose_predictor의 원-핫 인코딩, food_recognition의 기본값 산출,
# 라우터의 요청 검증까지 여러 계층에서 공유하므로 core에 둔다.
DIAGNOSIS_GROUPS = ["건강군", "전당뇨", "2형당뇨"]

# 식전 혈당 미입력 시 진단군별 기본값 (mg/dL)
# ADA/대한당뇨병학회 공복혈당 범위의 중간값 기준
#   건강군: 70~99  → 95
#   전당뇨: 100~125 → 115
#   2형당뇨: 126~168 → 140 (모델 신뢰구간 MAX_RELIABLE_BASELINE=168.8 이내)
# TODO(미결 정책): 노션 백엔드 가이드 10장 기준, 이 기본값 확정은 아직 팀 합의 전이다.
PRE_GLUCOSE_DEFAULTS = {
    "건강군": 95,
    "전당뇨": 115,
    "2형당뇨": 140,
}


def get_pre_glucose_default(diagnosis_group: str) -> int:
    """식전 혈당 미입력 시 진단군별 기본값 반환"""
    return PRE_GLUCOSE_DEFAULTS.get(diagnosis_group, 95)


def log_token_usage(response, label: str = ""):
    """Gemini 응답의 토큰 사용량을 터미널에 출력"""
    usage = getattr(response, "usage_metadata", None)
    if usage is None:
        print(f"[토큰 사용량{f' - {label}' if label else ''}] 정보 없음")
        return

    prompt_tokens = usage.prompt_token_count or 0
    output_tokens = usage.candidates_token_count or 0
    total_tokens = usage.total_token_count or 0

    print(
        f"[토큰 사용량{f' - {label}' if label else ''}] "
        f"입력: {prompt_tokens} / 출력: {output_tokens} / 합계: {total_tokens}"
    )
