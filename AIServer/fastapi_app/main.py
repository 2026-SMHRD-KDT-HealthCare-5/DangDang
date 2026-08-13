# -*- coding: utf-8 -*-
"""
당당이 챗봇 - 1단계 프로토타입 (Gemini API 버전)
벡터DB 없이 고정 지식(fixed knowledge)을 시스템 프롬프트에 넣어
Gemini API로 대화형 응답을 생성하는 최소 기능 버전.

* GL(혈당부하지수) 계산은 이 서비스 범위에서 제외 — 걷기/식후 관리 조언 중심으로만 구성

폴더 구조는 노션 "백엔드 가이드 (Spring Boot)" 5장 FastAPI 폴더 구조 스펙을 따른다:
    core/        환경설정, Gemini 클라이언트, 공용 상수
    schemas/     요청/응답 Pydantic 모델
    prompts/     Gemini에 넣는 프롬프트 문자열
    services/    비즈니스 로직 (대화, 음식 인식/재분석, 혈당 예측, 걷기 미션 계산)
    repositories/ 데이터 접근 (지금은 CSV, 추후 FOOD_INFO 테이블로 교체)
    routers/     HTTP 엔드포인트 — 이 계층은 얇게 유지하고 로직은 services에 위임

실행 (fastapi_app/ 디렉터리 안에서):
    pip install -r requirements.txt
    # fastapi_app/.env 에 GEMINI_API_KEY=발급받은키 저장
    uvicorn main:app --reload
"""

from fastapi import FastAPI
from fastapi.responses import JSONResponse

from routers import chat, reanalyze, recognize, predict

app = FastAPI(title="당당이 챗봇 프로토타입 (1단계, Gemini, RAG 연동)")

app.include_router(chat.router)
app.include_router(recognize.router)
app.include_router(reanalyze.router)
app.include_router(predict.router)


@app.get("/")
def health_check():
    return JSONResponse(
        content={"status": "ok", "service": "당당이 챗봇 프로토타입 (1단계, Gemini, RAG 연동)"},
        media_type="application/json; charset=utf-8",
    )
