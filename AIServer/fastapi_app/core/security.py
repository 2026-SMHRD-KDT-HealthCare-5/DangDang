# -*- coding: utf-8 -*-
"""
Spring -> FastAPI 내부 호출 인증.

노션 "백엔드 가이드" 원칙:
- FastAPI(/rag/*, /internal/*)는 Spring 서버만 호출할 수 있어야 한다.
- 클라이언트(Android 앱)가 FastAPI를 직접 부르는 경로는 없어야 하며,
  Nginx 등 배포 인프라로 막기 전까지는 이 헤더 검증이 유일한 방어선이다.
- X-Internal-Api-Key 헤더가 없거나 값이 틀리면 401로 거부한다 (NFR-DE-002).

사용법 (라우터에 의존성으로 붙이기):
    router = APIRouter(dependencies=[Depends(verify_internal_api_key)])
"""

import os
from fastapi import Header, HTTPException, status


def verify_internal_api_key(x_internal_api_key: str | None = Header(default=None)) -> None:
    # 모듈 임포트 시점이 아니라 요청 처리 시점에 읽는다 — core/config.py의
    # load_dotenv() 호출 순서와 무관하게 항상 최신 환경변수를 보도록.
    internal_api_key = os.environ.get("INTERNAL_API_KEY")

    if not internal_api_key:
        # 서버에 키 자체가 설정 안 돼있으면 무조건 막는다 (fail-closed).
        # 키를 안 걸어놓고 그냥 통과시켜버리면 인증이 있는 척만 하는 게 되므로,
        # 설정 누락은 "인증 없음"이 아니라 "서버 설정 오류"로 취급한다.
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="INTERNAL_API_KEY가 서버에 설정되지 않았습니다.",
        )
    if x_internal_api_key != internal_api_key:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="X-Internal-Api-Key가 없거나 올바르지 않습니다.",
        )
