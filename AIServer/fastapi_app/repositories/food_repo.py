# -*- coding: utf-8 -*-
"""
음식명 매칭 모듈

Gemini가 사진/자연어로 인식한 음식명(예: "치킨", "제육볶음")을
DB(food_info 테이블)에서 가장 유사한 항목과 매칭해서
탄수화물/단백질/지방/식이섬유(1 serving_size 기준, 100g 기준 아님)를 가져온다.

※ 매칭 방식이 두 가지로 나뉜다 (서버 시작할 때 DB 연결이 되는지 보고 자동 선택):


   1단계. 오프라인 개발  → 로컬 food_for_db.csv를 통째로 읽어서
     메모리에 올려두고 rapidfuzz로 유사도 매칭 (기존 방식, 폴백용으로 유지).
   FastAPI는 DB에 읽기(SELECT)만 하고, 쓰기(INSERT/UPDATE/DELETE)는 하지 않는다
   (Spring 담당).

    2단계.  DB 확장연결 → pg_trgm(문자열 유사도 검색용 PostgreSQL 확장)으로 DB 안에서
     직접 유사도 검색. 요청마다 SQL 쿼리 한 번씩 날림 (전체 데이터를 메모리에 안 올림).

   ⚠️ pg_trgm의 similarity() 점수(0~1을 100배한 값)는 rapidfuzz의 WRatio 점수와
      계산 방식이 달라서, 같은 문자열이어도 나오는 점수가 다를 수 있다.
      food_recognition.py의 MATCH_SCORE_THRESHOLD(=70)는 원래 rapidfuzz 기준으로
      잡은 값이라, DB 모드로 실제 서비스하기 전에 실제 검색 결과로 임계값을
      다시 확인/조정해보는 걸 권장.

매칭 전략
---------
1. 정확히 일치하는 식품명이 있으면 그걸 사용
2. 없으면 유사도 검색 (DB 모드: pg_trgm / CSV 모드: rapidfuzz)
3. brand가 주어지면, food_name에 브랜드가 "비비큐_황금올리브 치킨"처럼
   접두어로 포함되어 있으므로 그 브랜드가 포함된 항목으로 우선 필터링

목차
1. DEFAULT_CSV_PATH — DB 접속 실패 시 쓸 폴백용 food_for_db.csv 위치
2. SIZE_KEYWORD_TO_CODE / _extract_size_code() — "라지", "패밀리" 같은 한글 사이즈 단어를 영문 코드로 변환
3. FoodDB.__init__() — DB 연결 테스트 후 db/csv 모드 결정 (db면 연결만, csv면 전체 로드)
4. FoodDB.search() — 모드에 따라 _search_db()/_search_csv()로 위임
5. FoodDB._search_db() — pg_trgm similarity()로 DB에서 직접 유사도 검색
6. FoodDB._search_csv() — rapidfuzz로 메모리 위에서 유사도 검색 (기존 로직)
7. FoodDB._rows_to_result() / _row_to_dict() — 결과 행을 응답용 dict로 변환하는 내부 헬퍼
8. FoodDB.get_best_match() — 가장 유사한 항목 1건만 반환 (브랜드 불일치 시 전체 재검색까지 처리)
9. food_db — 모듈이 처음 로딩될 때 1회만 만들어지는 싱글턴 (요청마다 새로 안 만듦)
"""

import re
from pathlib import Path

import pandas as pd
from rapidfuzz import fuzz, process
from sqlalchemy import text

from core.config import db_engine

# DB 접속이 안 될 때(오프라인 개발 등)를 대비한 폴백용 CSV.
# __file__ 기준 상대경로라 uvicorn을 어느 위치에서 실행해도 항상 같은 파일을 찾는다.
DEFAULT_CSV_PATH = Path(__file__).resolve().parent.parent / "data" / "food_for_db.csv"

# food_info 테이블/CSV 공통 컬럼 (SELECT 절, DataFrame 컬럼 이름을 이걸로 통일)
FOOD_TABLE_COLUMNS = [
    "food_no", "food_name", "calorie", "carb", "protein", "fat",
    "fiber", "serving_size", "sugar",
]

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
        self.mode = "csv"
        self.df = None

        if db_engine is not None:
            try:
                with db_engine.connect() as conn:
                    conn.execute(text("SELECT 1"))
                self.mode = "db"
                print("[food_repo] DB 연결 확인 완료 - pg_trgm 기반 검색 사용 (food_info 테이블)")
                return
            except Exception as e:
                print(f"[food_repo] DB 연결 실패, CSV로 폴백: {e}")

        # DB 접속 정보가 없거나(.env 미설정) 접속 자체가 실패한 경우의 폴백
        try:
            self.df = pd.read_csv(csv_path, encoding="utf-8-sig")
        except UnicodeDecodeError:
            self.df = pd.read_csv(csv_path, encoding="cp949")
        print(f"[food_repo] CSV({csv_path})에서 {len(self.df)}건 로드 완료 (rapidfuzz 기반 검색)")

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

        if self.mode == "db":
            return self._search_db(query, brand, top_k, size_code)
        return self._search_csv(query, brand, top_k, size_code)

    def _search_db(self, query: str, brand: str | None, top_k: int, size_code: str | None):
        """pg_trgm의 similarity()로 DB 안에서 직접 유사도 검색"""
        columns_sql = ", ".join(FOOD_TABLE_COLUMNS)

        with db_engine.connect() as conn:
            # 1) 정확 일치 우선
            sql = f"SELECT {columns_sql} FROM food_info WHERE food_name = :q"
            params = {"q": query, "k": top_k}
            if brand:
                sql += " AND food_name LIKE :brand"
                params["brand"] = f"%{brand}%"
            sql += " LIMIT :k"
            rows = conn.execute(text(sql), params).mappings().all()
            if rows:
                return [self._row_to_dict(r, match_type="정확일치", score=100) for r in rows]

            # 2) pg_trgm 유사도 매칭
            # 사이즈코드가 감지됐으면, food_name이 "(코드)"로 끝나는 항목을 최우선 정렬
            order_by = "similarity(food_name, :q) DESC"
            if size_code:
                order_by = (
                    f"CASE WHEN food_name ~ '\\({re.escape(size_code)}\\)\\s*$' THEN 0 ELSE 1 END, "
                    + order_by
                )

            sql = f"""
                SELECT {columns_sql}, similarity(food_name, :q) AS score
                FROM food_info
                WHERE similarity(food_name, :q) > 0.1
            """
            params = {"q": query, "k": top_k}
            if brand:
                sql += " AND food_name LIKE :brand"
                params["brand"] = f"%{brand}%"
            sql += f" ORDER BY {order_by} LIMIT :k"

            rows = conn.execute(text(sql), params).mappings().all()

        return [
            self._row_to_dict(r, match_type="유사도매칭", score=r["score"] * 100)
            for r in rows
        ]

    def _search_csv(self, query: str, brand: str | None, top_k: int, size_code: str | None):
        """rapidfuzz로 메모리(CSV로 읽어둔 DataFrame) 위에서 유사도 매칭 (DB 접속 실패 시 폴백)"""
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
