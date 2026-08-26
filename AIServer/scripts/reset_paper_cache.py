# -*- coding: utf-8 -*-
"""
논문 컨텍스트 캐시를 안전하게 초기화하는 스크립트.

프롬프트(PAPER_QA_INSTRUCTION)나 papers_combined.txt 내용을 바꾼 뒤엔 캐시를
다시 만들어야 하는데, ".paper_cache_name 로컬 파일만 지우고 서버 재시작"하면
안 된다 — 그건 우리 쪽 "이 캐시 이름 기억" 메모만 지우는 거지, Gemini 서버에
실제로 저장된 캐시(그리고 그 저장 비용)는 그대로 남는다.
(2026-08-13: 이 실수로 캐시가 안 지워진 채 7개나 쌓여서 저장 비용이
계속 나갔던 사고가 있었음 — 자세한 경위는 CLAUDE.md 참고)

이 스크립트는:
1. 이 API 키로 만들어진 살아있는 캐시를 전부 조회
2. 전부 client.caches.delete()로 실제 삭제 (서버 쪽 데이터 + 저장비용 종료)
3. 로컬 .paper_cache_name 파일도 같이 삭제

사용법 (fastapi_app/ 디렉터리 기준):
    cd AIServer/fastapi_app
    python ../scripts/reset_paper_cache.py
"""

import sys
from pathlib import Path

# fastapi_app/을 import 경로에 추가 (core.config 등을 쓰기 위함)
FASTAPI_APP_DIR = Path(__file__).resolve().parent.parent / "fastapi_app"
sys.path.insert(0, str(FASTAPI_APP_DIR))

from core.config import client  # noqa: E402

CACHE_NAME_PATH = FASTAPI_APP_DIR / "services" / "rag" / ".paper_cache_name"


def main():
    caches = list(client.caches.list())
    print(f"살아있는 캐시: {len(caches)}개")

    for c in caches:
        try:
            client.caches.delete(name=c.name)
            print(f"  삭제 완료: {c.name} ({c.usage_metadata.total_token_count if c.usage_metadata else '?'} 토큰)")
        except Exception as e:
            print(f"  삭제 실패: {c.name} - {e}")

    if CACHE_NAME_PATH.exists():
        CACHE_NAME_PATH.unlink()
        print(f"로컬 파일 삭제: {CACHE_NAME_PATH}")
    else:
        print("로컬 파일 없음 (이미 삭제됐거나 아직 안 만들어짐)")

    remaining = list(client.caches.list())
    print(f"완료. 남은 캐시: {len(remaining)}개")


if __name__ == "__main__":
    main()
