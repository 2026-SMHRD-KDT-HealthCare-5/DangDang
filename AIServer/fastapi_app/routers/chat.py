# -*- coding: utf-8 -*-
from fastapi import APIRouter
from fastapi.responses import JSONResponse

from schemas.chat import ChatRequest
from services.rag_chat import answer_chat

router = APIRouter()


@router.post("/rag/chat")
def chat(req: ChatRequest):
    """
    일반 대화 엔드포인트 (Spring이 /api/chat -> 여기로 내부 호출)

    음식 인식/혈당 예측은 이 엔드포인트의 역할이 아님 — 그건
    /rag/intake-logs/recognize, /rag/intake-logs/reanalyze,
    /rag/intake-logs/predict가 전담한다. 여긴 순수 대화(페르소나 응답,
    논문 기반 Q&A)만 처리한다.
    """
    reply = answer_chat(req.user_id, req.message, req.diagnosis_group)

    # PowerShell(Windows) 등 일부 클라이언트가 charset 없는 응답을
    # 잘못 해석해 한글이 깨지는 문제 방지 위해 charset 명시
    return JSONResponse(
        content={"reply": reply},
        media_type="application/json; charset=utf-8",
    )
