# -*- coding: utf-8 -*-
"""
food_db_imputed.csv (식약처 표준데이터 + 결측치 보완본) →
실제 서비스 DB 스키마(food 테이블)에 맞게 변환

목표 스키마 (8개 컬럼)
----------------------
food_no       integer          -- PK, 1부터 순번
food_name     varchar          -- 식품명
calorie       numeric(6,2)     -- 100g당 kcal
carb          numeric(6,2)     -- 100g당 탄수화물(g)
protein       numeric(6,2)     -- 100g당 단백질(g)
fat           numeric(6,2)     -- 100g당 지방(g)
fiber         numeric(6,2)     -- 100g당 식이섬유(g)
serving_size  integer          -- 1인분 기준 중량(g), CSV의 '식품중량'에서 추출

사용법
------
    python transform_to_db_schema.py food_db_imputed.csv food_for_db.csv
"""

import sys
import re
import pandas as pd

# 음료/차류는 당당이 서비스 범위에서 제외하기로 함 (결측치도 심하고 서비스 목적과도 안 맞음)
EXCLUDED_CATEGORIES = ["음료 및 차류"]

SOURCE_TO_TARGET = {
    "식품명": "food_name",
    "에너지(kcal)": "calorie",
    "탄수화물(g)": "carb",
    "단백질(g)": "protein",
    "지방(g)": "fat",
    "식이섬유(g)": "fiber",
}


def extract_grams(text) -> float | None:
    """'930g', '1640g' 같은 문자열에서 숫자만 추출. 실패하면 None."""
    if pd.isna(text):
        return None
    match = re.search(r"([\d.]+)\s*g", str(text))
    return float(match.group(1)) if match else None


def load_food_db(path: str) -> pd.DataFrame:
    try:
        return pd.read_csv(path, encoding="utf-8-sig")
    except UnicodeDecodeError:
        return pd.read_csv(path, encoding="cp949")


def build_food_name(row) -> str:
    """업체명이 있으면 '브랜드_음식명' 형태로 합쳐서 구분 가능하게 만듦.
    '해당없음'은 실제 브랜드가 없다는 뜻(진짜 결측 아님)이라 접두어를 붙이지 않음."""
    brand = row.get("업체명")
    name = row["식품명"]
    if pd.notna(brand) and str(brand).strip() and str(brand).strip() != "해당없음":
        return f"{brand}_{name}"
    return name


def transform(df: pd.DataFrame, exclude_beverages: bool = True) -> pd.DataFrame:
    if exclude_beverages:
        before = len(df)
        df = df[~df["식품대분류명"].isin(EXCLUDED_CATEGORIES)].copy()
        print(f"음료/차류 제외: {before}건 -> {len(df)}건")

    out = pd.DataFrame()
    out["food_name"] = df.apply(build_food_name, axis=1)
    out["calorie"] = df["에너지(kcal)"]
    out["carb"] = df["탄수화물(g)"]
    out["protein"] = df["단백질(g)"]
    out["fat"] = df["지방(g)"]
    out["fiber"] = df["식이섬유(g)"]

    serving = df["식품중량"].apply(extract_grams)
    missing_serving = serving.isna().sum()
    if missing_serving > 0:
        print(f"serving_size 추출 실패 {missing_serving}건 -> 기본값 100g으로 대체")
    out["serving_size"] = serving.fillna(100).round().astype(int)

    # food_no: 1부터 순번 부여 (DB에서 SERIAL로 자동 채번한다면 이 컬럼은 빼고 insert해도 됨)
    out.insert(0, "food_no", range(1, len(out) + 1))

    # numeric(6,2) 범위 초과 방지 (최대 9999.99) - 혹시 모를 이상치 클리핑
    for col in ["calorie", "carb", "protein", "fat", "fiber"]:
        out[col] = out[col].clip(upper=9999.99).round(2)

    out = out.reset_index(drop=True)
    out["food_no"] = range(1, len(out) + 1)

    # 결측치 보완(impute_nutrition.py) 후에도 못 채운 극소수 항목은 제외
    before_dropna = len(out)
    out = out.dropna(subset=["calorie", "carb", "protein", "fat", "fiber"]).reset_index(drop=True)
    dropped = before_dropna - len(out)
    if dropped > 0:
        print(f"영양성분 결측 남은 항목 {dropped}건 제외")

    # 식품명 중복 제거: 같은 이름이면 맨 위(첫 번째) 것만 남김
    before_dedup = len(out)
    out = out.drop_duplicates(subset=["food_name"], keep="first").reset_index(drop=True)
    dedup_removed = before_dedup - len(out)
    if dedup_removed > 0:
        print(f"중복 식품명 {dedup_removed}건 제거 (첫 번째만 유지)")

    out["food_no"] = range(1, len(out) + 1)  # 제외/중복제거 후 순번 재부여

    return out


def main():
    if len(sys.argv) < 3:
        print("사용법: python transform_to_db_schema.py 원본.csv 결과.csv")
        sys.exit(1)

    input_path, output_path = sys.argv[1], sys.argv[2]

    df = load_food_db(input_path)
    print(f"원본: {len(df)}건")

    result = transform(df)
    print(f"변환 결과: {len(result)}건, 컬럼: {result.columns.tolist()}")

    result.to_csv(output_path, index=False, encoding="utf-8-sig")
    print(f"저장 완료: {output_path}")


if __name__ == "__main__":
    main()
