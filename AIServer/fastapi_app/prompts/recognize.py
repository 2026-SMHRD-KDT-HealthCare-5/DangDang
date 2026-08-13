# -*- coding: utf-8 -*-
"""
POST /rag/intake-logs/recognize 에서 쓰는 Gemini 프롬프트 — "이 음식이 뭔지 알아맞혀줘" 담당.

목차
1. FOOD_RECOGNITION_PROMPT — 사진을 보고 음식명을 알아내는 프롬프트
2. TEXT_FOOD_EXTRACTION_PROMPT — 채팅 텍스트에서 음식명을 뽑아내는 프롬프트
"""

FOOD_RECOGNITION_PROMPT = """이 사진 속 음식을 인식해서 아래 JSON 형식으로만 답해.
설명 문장이나 마크다운 코드블록(```json) 없이, 순수 JSON 텍스트만 출력해.

{
  "food_name": "음식 이름 (한글, 검색하기 좋게 간결하게. 예: '황금올리브 치킨', '불고기 피자')",
  "brand": "프랜차이즈/브랜드명이 보이면 적고, 안 보이면 null",
  "confidence": "high | medium | low 중 하나 (인식 확신도)",
  "estimated_serving_g": 예상 1인분 중량(그램, 숫자만)
}

사진에서 음식이 명확히 보이지 않으면 food_name을 "인식불가"로 설정해.
"""

TEXT_FOOD_EXTRACTION_PROMPT = """사용자 메시지에서 음식명을 추출해서 아래 JSON 형식으로만 답해.
설명 문장이나 마크다운 코드블록(```json) 없이, 순수 JSON 텍스트만 출력해.

{
  "food_name": "음식 이름 (한글, 검색하기 좋게 간결하게. 예: '김치찌개', '제육볶음')",
  "brand": "프랜차이즈/브랜드명이 언급됐으면 적고, 없으면 null"
}

음식이 언급되지 않았으면 food_name을 "인식불가"로 설정해.
"""
