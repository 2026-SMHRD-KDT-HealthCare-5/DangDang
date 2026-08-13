# -*- coding: utf-8 -*-
"""
음식명 매칭 모듈

Gemini가 사진/자연어로 인식한 음식명(예: "치킨", "제육볶음")을
실제 서비스 DB 스키마와 동일한 food_for_db.csv에서 가장 유사한 항목과 매칭해서
탄수화물/단백질/지방/식이섬유(100g 기준)를 가져온다.

※ transform_to_db_schema.py로 만든 food_for_db.csv를 그대로 씀.
   (예전엔 food_db_imputed.csv를 따로 썼는데, DB 적재용 파일과 전처리가 달라서
    불일치가 생겼음 -> 이제 하나의 소스만 사용해서 그 문제를 없앰)

매칭 전략
---------
1. 정확히 일치하는 식품명이 있으면 그걸 사용
2. 없으면 rapidfuzz로 식품명 전체에 대해 유사도 검색 (여러 후보 반환 가능)
3. brand가 주어지면, food_name에 브랜드가 "비비큐_황금올리브 치킨"처럼
   접두어로 포함되어 있으므로 그 브랜드가 포함된 항목으로 우선 필터링
"""

import re
from pathlib import Path

import pandas as pd
from rapidfuzz import fuzz, process

# fastapi_app/data/food_for_db.csv 고정 위치.
# __file__ 기준 상대경로라 uvicorn을 어느 위치에서 실행해도 항상 같은 파일을 찾는다.
# TODO: 현재는 CSV 기반 텍스트 유사도 매칭 프로토타입. 스펙(백엔드 가이드 5장)상
#       최종 목표는 PostgreSQL FOOD_INFO 테이블 + pgvector/pg_trgm 유사도 검색으로 교체.
DEFAULT_CSV_PATH = Path(__file__).resolve().parent.parent / "data" / "food_for_db.csv"

# 한글 사이즈 표현 -> 데이터에 실제로 쓰이는 영문 코드 매핑
# (파파존스 등 피자 프랜차이즈 표기 관례 기준. 브랜드마다 F/P 의미가 조금 다를 수 있어
#  100% 확신은 못 하니, 실제 대응 안 맞으면 이 매핑부터 의심할 것)
SIZE_KEYWORD_TO_CODE = {
    "엑스라지": "XL",
    "엑스 라지": "XL",
    "라지": "L",
    "레귤러": "R",
    "미디엄": "M",
    "패밀리": "F",
    "파티": "P",
}
# 긴 키워드부터 검사해야 "엑스라지" 안의 "라지"가 먼저 걸리는 걸 방지
_SIZE_KEYWORDS_SORTED = sorted(SIZE_KEYWORD_TO_CODE.keys(), key=len, reverse=True)


def _extract_size_code(query: str):
    """검색어에서 한글 사이즈 단어를 찾아 (정제된 검색어, 사이즈코드) 반환. 없으면 (원본, None)"""
    for kw in _SIZE_KEYWORDS_SORTED:
        if kw in query:
            cleaned = query.replace(kw, "").strip()
            return cleaned, SIZE_KEYWORD_TO_CODE[kw]
    return query, None


class FoodDB:
    def __init__(self, csv_path: Path | str = DEFAULT_CSV_PATH):
        try:
            self.df = pd.read_csv(csv_path, encoding="utf-8-sig")
        except UnicodeDecodeError:
            self.df = pd.read_csv(csv_path, encoding="cp949")

    def search(self, query: str, brand: str | None = None, top_k: int = 5):
        """
        query: 검색할 음식명 (예: "치킨", "황금올리브 치킨"). "라지", "패밀리" 같은
               한글 사이즈 단어는 자동으로 감지해서 사이즈 매칭에 반영됨
        brand: 특정 프랜차이즈로 좁히고 싶을 때 (예: "비비큐"). food_name 접두어로 필터링
        top_k: 유사도 상위 몇 개 후보를 반환할지

        반환: [{식품명, 탄수화물, 단백질, 지방, 식이섬유, 에너지, serving_size, 유사도점수, 매칭방식}, ...]
        """
        # 검색어에 "라지", "패밀리" 같은 한글 사이즈 단어가 있으면 분리해서
        # 매칭에는 사이즈 단어를 빼고(텍스트 유사도 왜곡 방지), 이후 결과 정렬에 사이즈코드로 반영
        cleaned_query, size_code = _extract_size_code(query)
        query = cleaned_query if cleaned_query else query

        candidates_df = self.df
        if brand:
            filtered = self.df[self.df["food_name"].astype(str).str.contains(brand, na=False)]
            if len(filtered) > 0:
                candidates_df = filtered

        # 1) 정확 일치 우선 (사이즈 단어 뗀 이름으로 정확히 일치하는 게 있으면 그걸 우선)
        exact = candidates_df[candidates_df["food_name"] == query]
        if len(exact) > 0:
            return self._rows_to_result(exact.head(top_k), match_type="정확일치", score=100)

        # 2) 부분문자열 필터 → 유사도 매칭
        #    쿼리가 food_name에 포함된 항목이 있으면 그 안에서만 퍼지 매칭
        #    (짧은 쿼리 "치킨"이 "치킨가스"보다 "KFC_치킨_오리지널" 같은 항목을 찾게)
        _strip = lambda s: re.sub(r"[_ ]+", "", s) if isinstance(s, str) else s
        norm_query = _strip(query)
        substr_df = candidates_df[
            candidates_df["food_name"].apply(lambda n: norm_query in _strip(str(n)))
        ]
        fuzzy_df = substr_df if len(substr_df) > 0 else candidates_df

        names = fuzzy_df["food_name"].tolist()
        matches = process.extract(query, names, scorer=fuzz.WRatio, processor=_strip, limit=None)
        # matches: [(원본_food_name, score, index_in_names), ...]

        def sort_key(m):
            matched_name, score, _ = m
            # 사이즈코드가 감지됐으면, food_name이 "(코드)"로 끝나는 항목을 최우선으로
            size_match_bonus = 1 if size_code and re.search(rf"\({size_code}\)\s*$", matched_name) else 0
            return (-size_match_bonus, -score, len(matched_name))

        matches = sorted(matches, key=sort_key)[:top_k]

        results = []
        for matched_name, score, _ in matches:
            row = fuzzy_df[fuzzy_df["food_name"] == matched_name].iloc[0]
            results.append(self._row_to_dict(row, match_type="유사도매칭", score=score))
        return results

    def _rows_to_result(self, rows_df: pd.DataFrame, match_type: str, score: float):
        return [self._row_to_dict(row, match_type, score) for _, row in rows_df.iterrows()]

    def _row_to_dict(self, row, match_type: str, score: float):
        return {
            "food_no": int(row["food_no"]),
            "food_name": str(row["food_name"]),
            "calorie": float(row["calorie"]),
            "carb": float(row["carb"]),
            "protein": float(row["protein"]),
            "fat": float(row["fat"]),
            "fiber": float(row["fiber"]),
            "sugar": float(row["sugar"]),
            "serving_size": int(row["serving_size"]),
            "match_type": match_type,
            "match_score": round(float(score), 1),
        }

    def get_best_match(self, query: str, brand: str | None = None):
        """가장 유사한 항목 1개만 반환. 매칭 결과 없으면 None.

        brand 처리 전략:
        1. 브랜드가 DB에 접두어로 존재하면(KFC_, 비비큐_ 등) → 브랜드 결과 반환
           (점수가 threshold 미만이면 main.py에서 matched=false 처리)
        2. 브랜드가 접두어로 없으면(BBQ→비비큐 등 불일치) → 전체 데이터에서 재검색
        """
        results = self.search(query, brand=brand, top_k=1)
        best = results[0] if results else None

        if brand and best:
            brand_upper = brand.upper()
            name_upper = best["food_name"].upper()
            brand_confirmed = name_upper.startswith(brand_upper + "_") or name_upper.startswith(brand_upper)

            if not brand_confirmed:
                # 브랜드 불일치 (예: "BBQ" → "비비큐") → 전체에서 재검색
                full_results = self.search(query, brand=None, top_k=1)
                if full_results and full_results[0]["match_score"] > best["match_score"]:
                    best = full_results[0]

        return best


# 모듈 최초 import 시 1회만 로드되어 이후 재사용된다 (Python import 캐싱).
# services/food_recognition.py가 이 싱글턴을 그대로 가져다 쓴다.
food_db = FoodDB()


if __name__ == "__main__":
    # 간단한 테스트
    db = food_db

    tests = [
        ("황금올리브 치킨", "비비큐"),
        ("불고기피자", None),
        ("김치찌개", None),
    ]
    for query, brand in tests:
        print(f"\n검색어: '{query}' (브랜드: {brand})")
        for r in db.search(query, brand=brand, top_k=3):
            print(f"  {r}")