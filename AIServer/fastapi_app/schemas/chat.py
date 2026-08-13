# -*- coding: utf-8 -*-
"""POST /rag/chat 요청/응답 스키마"""

from pydantic import BaseModel


class ChatRequest(BaseModel):
    user_id: str
    message: str
    diagnosis_group: str | None = None  # 넘어오면 해당 유저의 진단군으로 저장


class ChatResponse(BaseModel):
    reply: str
