# -*- coding: utf-8 -*-
"""
당당이 챗봇 서버의 시작점(entry point). 이 파일 하나로 서버가 켜진다.

목차
1. app 생성 — FastAPI(title=...) 인스턴스 생성
2. 라우터 등록 — chat / recognize / reanalyze / predict, 4개 라우터를 app에 연결
3. health_check() — GET / : 서버가 살아있는지 확인하는 헬스체크 엔드포인트

* GL(혈당부하지수) 계산은 이 서비스 범위에서 제외 — 걷기/식후 관리 조언 중심으로만 구성

폴더 구조는 노션 "백엔드 가이드 (Spring Boot)" 5장 FastAPI 폴더 구조 스펙을 따른다:
    1. core/         환경설정, Gemini 클라이언트, 공용 상수
    2. schemas/      요청/응답 Pydantic 모델
    3. prompts/      Gemini에 넣는 프롬프트 문자열
    4. services/     비즈니스 로직 (대화, 음식 인식/재분석, 혈당 예측, 걷기 미션 계산)
    5. repositories/ 데이터 접근 (지금은 CSV, 추후 FOOD_INFO 테이블로 교체)
    6. routers/      HTTP 엔드포인트 — 이 계층은 얇게 유지하고 로직은 services에 위임

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
