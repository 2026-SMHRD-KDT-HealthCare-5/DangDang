# -*- coding: utf-8 -*-
"""
전역 설정값 모음: 환경변수 로드, Gemini 클라이언트 생성, 서비스 전반에서
공유하는 상수(모델명, 진단군, 식전 혈당 기본값)와 공용 유틸(log_token_usage).

core/는 다른 계층(services, repositories, routers)이 참조하는 최하위 계층이다.
core가 services나 routers를 import하는 일은 없어야 한다 (역방향 의존 금지).
"""

import os
from pathlib import Path

from dotenv import load_dotenv
from google import genai

# fastapi_app/ 루트 기준으로 .env를 찾는다.
# uvicorn을 다른 위치(예: AIServer/)에서 실행해도 항상 같은 파일을 읽도록
# __file__ 기준 상대경로로 고정한다 (os.getcwd() 기준이면 실행 위치에 따라 깨짐).
APP_ROOT = Path(__file__).resolve().parent.parent
load_dotenv(APP_ROOT / ".env")

GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY")
client = genai.Client(api_key=GEMINI_API_KEY)

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
