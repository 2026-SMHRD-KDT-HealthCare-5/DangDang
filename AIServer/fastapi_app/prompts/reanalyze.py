# -*- coding: utf-8 -*-
"""POST /rag/intake-logs/reanalyze 에서 쓰는 Gemini 프롬프트"""

FOOD_REANALYSIS_IMAGE_PROMPT = """이 사진 속 음식을 분석해서 아래 JSON 형식으로만 답해.
설명 문장이나 마크다운 코드블록(```json) 없이, 순수 JSON 텍스트만 출력해.

{
  "food_name": "음식 이름 (한글, 간결하게)",
  "serving_size": 예상 1인분 중량(그램, 숫자만),
  "nutrition": {
    "carb": 100g당_탄수화물_그램_숫자,
    "sugar": 100g당_당류_그램_숫자,
    "protein": 100g당_단백질_그램_숫자,
    "fat": 100g당_지방_그램_숫자,
    "fiber": 100g당_식이섬유_그램_숫자,
    "calorie": 100g당_칼로리_숫자
  }
}

사진을 정밀하게 분석해서 음식 종류를 판별하고, 일반적인 조리법과 재료를 기준으로
영양성분을 최대한 합리적으로 추정해.
"""

FOOD_REANALYSIS_TEXT_PROMPT = """다음 음식의 영양성분을 추정해서 아래 JSON 형식으로만 답해.
설명 문장이나 마크다운 코드블록(```json) 없이, 순수 JSON 텍스트만 출력해.

{
  "food_name": "음식 이름 (한글, 간결하게)",
  "serving_size": 예상 1인분 중량(그램, 숫자만),
  "nutrition": {
    "carb": 100g당_탄수화물_그램_숫자,
    "sugar": 100g당_당류_그램_숫자,
    "protein": 100g당_단백질_그램_숫자,
    "fat": 100g당_지방_그램_숫자,
    "fiber": 100g당_식이섬유_그램_숫자,
    "calorie": 100g당_칼로리_숫자
  }
}

해당 음식의 일반적인 조리법과 재료를 기준으로 영양성분을 최대한 합리적으로 추정해.
"""
