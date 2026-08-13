# -*- coding: utf-8 -*-
"""
POST /rag/intake-logs/predict 요청 스키마 — 음식 최종 확정 직전에 쓰는 요청 형식.

목차
1. PortionPredictRequest — 영양성분 + 섭취비율(portion) + 식전혈당(baseline) + 진단군
"""

from pydantic import BaseModel


class PortionPredictRequest(BaseModel):
    """
    nutrition(carb/sugar/protein/fat/fiber/calorie)은 "1 serving_size 기준" 값으로
    받는다 (100g 기준이 아님 — Spring이 FOOD_INFO/CUSTOM_FOOD에서 가져온 값을 그대로
    전달). portion을 곱해서 실제 섭취량을 반영한다.
    """
    carb: float
    sugar: float = 0
    protein: float
    fat: float
    fiber: float
    calorie: float = 0
    portion: float = 1.0  # 섭취 비율 (예: 0.5 / 1.0 / 1.5)
    baseline: float  # 식전 혈당 (preGlucose)
    diagnosis_group: str  # "건강군" | "전당뇨" | "2형당뇨"
