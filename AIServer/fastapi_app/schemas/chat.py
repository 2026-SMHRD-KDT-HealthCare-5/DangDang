# -*- coding: utf-8 -*-
"""
POST /rag/chat 요청/응답 스키마 — 여기 적힌 형식대로만 Spring이 보내고, 이 형식대로만 답해준다.

목차
1. ChatRequest — Spring이 보내는 요청 형식 (누가, 무슨 메시지, 진단군)
2. ChatResponse — 우리가 돌려주는 응답 형식 (챗봇 답변 문자열)

※ user_no는 DB의 USER 테이블 PK(정수)와 타입/이름을 맞춘 필드다.
   예전에는 user_id: str로 되어 있었는데, DB 컬럼은 정수(user_no)라서
   Spring이 실제로 숫자를 보내면 str 필드에서는 검증 오류가 날 수 있었다 — 타입을 맞춰서 수정함.
"""

from pydantic import BaseModel


class ChatRequest(BaseModel):
    user_no: int
    message: str
    diagnosis_group: str | None = None  # 넘어오면 해당 유저의 진단군으로 저장


class ChatResponse(BaseModel):
    reply: str
