# -*- coding: utf-8 -*-
"""
POST /rag/chat 요청/응답 스키마 — 여기 적힌 형식대로만 Spring이 보내고, 이 형식대로만 답해준다.

목차
1. ChatTurn — 대화 1턴(사용자 질문 + 당당이 답변) 형식
2. ChatRequest — Spring이 보내는 요청 형식 (누가, 무슨 메시지, 진단군, 최근 대화 이력)
3. ChatResponse — 우리가 돌려주는 응답 형식 (챗봇 답변 문자열)

※ user_no는 DB의 USER 테이블 PK(정수)와 타입/이름을 맞춘 필드다.
   예전에는 user_id: str로 되어 있었는데, DB 컬럼은 정수(user_no)라서
   Spring이 실제로 숫자를 보내면 str 필드에서는 검증 오류가 날 수 있었다 — 타입을 맞춰서 수정함.

※ (추가 2026-08-24, 버그 9) history 필드 — 예전엔 매 요청을 완전히 독립적으로 처리해서
   방금 무슨 대화를 했는지 전혀 기억을 못 했다(대화가 뚝뚝 끊기는 느낌의 원인).
   대화 이력의 원본은 Spring의 ai_chat 테이블이라서, Spring이 최근 몇 턴을 조회해서
   매 요청마다 같이 실어 보내주면 그걸로 맥락을 잡는다. FastAPI 자체는 여전히
   상태를 안 들고 있다(무상태 유지 — 이 프로젝트의 기본 원칙).
"""

from pydantic import BaseModel


class ChatTurn(BaseModel):
    user_message: str | None = None
    ai_message: str | None = None


class ChatRequest(BaseModel):
    user_no: int
    message: str
    diagnosis_group: str | None = None  # 넘어오면 해당 유저의 진단군으로 저장
    history: list[ChatTurn] = []  # 최근 대화 턴 (오래된 순). Spring이 ai_chat에서 조회해서 보냄


class ChatResponse(BaseModel):
    reply: str
