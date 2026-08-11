# -*- coding: utf-8 -*-
"""
특정 검색어에 대해 어떤 후보들이 몇 점으로 매칭되는지 확인하는 테스트 스크립트

사용법:
    python test_search.py "황금올리브"
    python test_search.py "황금올리브" 비비큐
"""

import sys
from food_lookup import FoodDB

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print('사용법: python test_search.py "검색어" [브랜드]')
        sys.exit(1)

    query = sys.argv[1]
    brand = sys.argv[2] if len(sys.argv) > 2 else None

    db = FoodDB("db_import/food_for_db.csv")

    print(f"검색어: '{query}'" + (f" (브랜드: {brand})" if brand else ""))
    print("-" * 60)

    results = db.search(query, brand=brand, top_k=10)
    for i, r in enumerate(results, 1):
        print(f"{i}. [{r['유사도점수']}점] {r['식품명']}")
        print(f"   탄수화물 {r['탄수화물']}g / 단백질 {r['단백질']}g / 지방 {r['지방']}g / 식이섬유 {r['식이섬유']}g")

    print()
    print(f"→ get_best_match()가 실제로 고르는 것: {db.get_best_match(query, brand=brand)['식품명']}")
