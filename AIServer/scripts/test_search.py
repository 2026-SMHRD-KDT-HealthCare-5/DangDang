# -*- coding: utf-8 -*-
"""
특정 검색어에 대해 어떤 후보들이 몇 점으로 매칭되는지 확인하는 테스트 스크립트

사용법 (AIServer/ 어느 위치에서 실행해도 됨):
    python scripts/test_search.py "황금올리브"
    python scripts/test_search.py "황금올리브" 비비큐

기존 버전은 food_lookup.FoodDB가 예전에 반환하던 한글 키(식품명/탄수화물 등)
기준으로 짜여 있었는데, 실제 FoodDB._row_to_dict()는 food_name/carb/protein/fat/fiber/
match_score 같은 영문 키를 반환해서 이 스크립트 자체가 KeyError로 깨져 있었음 — 이번에
repositories.food_repo의 실제 반환 키에 맞춰 고침.
"""

import sys
from pathlib import Path

# scripts/는 fastapi_app/ 밖에 있으므로, repositories 패키지를 import하려면
# fastapi_app/을 sys.path에 직접 추가해야 한다.
FASTAPI_APP_DIR = Path(__file__).resolve().parent.parent / "fastapi_app"
sys.path.insert(0, str(FASTAPI_APP_DIR))

from repositories.food_repo import FoodDB  # noqa: E402

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print('사용법: python scripts/test_search.py "검색어" [브랜드]')
        sys.exit(1)

    query = sys.argv[1]
    brand = sys.argv[2] if len(sys.argv) > 2 else None

    db = FoodDB()  # 기본 경로: fastapi_app/data/food_for_db.csv

    print(f"검색어: '{query}'" + (f" (브랜드: {brand})" if brand else ""))
    print("-" * 60)

    results = db.search(query, brand=brand, top_k=10)
    for i, r in enumerate(results, 1):
        print(f"{i}. [{r['match_score']}점] {r['food_name']}")
        print(f"   탄수화물 {r['carb']}g / 단백질 {r['protein']}g / 지방 {r['fat']}g / 식이섬유 {r['fiber']}g")

    print()
    best = db.get_best_match(query, brand=brand)
    print(f"→ get_best_match()가 실제로 고르는 것: {best['food_name'] if best else '(매칭 없음)'}")
