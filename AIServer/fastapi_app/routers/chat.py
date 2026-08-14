# -*- coding: utf-8 -*-
"""
POST /rag/chat 라우터 — Spring이 사용자 채팅 메시지를 받으면 내부적으로 이 엔드포인트를 호출한다.

목차
1. chat() — 요청을 받아 services/rag_chat.py의 answer_chat()에 위임하고 결과를 JSON으로 반환
"""
from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from core.security import verify_internal_api_key
from schemas.chat import ChatRequest
from services.rag_chat import answer_chat

router = APIRouter(dependencies=[Depends(verify_internal_api_key)])


@router.post("/rag/chat")
def chat(req: ChatRequest):
    """
    일반 대화 엔드포인트 (Spring이 /api/chat -> 여기로 내부 호출)

    음식 인식/혈당 예측은 이 엔드포인트의 역할이 아님 — 그건
    /rag/intake-logs/recognize, /rag/intake-logs/reanalyze,
    /rag/intake-logs/predict가 전담한다. 여긴 순수 대화(페르소나 응답,
    논문 기반 Q&A)만 처리한다.
    """
    reply = answer_chat(req.user_no, req.message, req.diagnosis_group)

    # PowerShell(Windows) 등 일부 클라이언트가 charset 없는 응답을
    # 잘못 해석해 한글이 깨지는 문제 방지 위해 charset 명시
    return JSONResponse(
        content={"reply": reply},
        media_type="application/json; charset=utf-8",
    )
