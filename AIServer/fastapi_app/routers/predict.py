# -*- coding: utf-8 -*-
from fastapi import APIRouter
from fastapi.responses import JSONResponse

from core.config import DIAGNOSIS_GROUPS
from schemas.predict import PortionPredictRequest
from services.glucose_predictor import glucose_predictor
from services.mission_calculator import calc_walking_mission

router = APIRouter()


@router.post("/rag/intake-logs/predict")
def predict_with_portion(req: PortionPredictRequest):
    """
    음식 최종 확정 시 portion 반영 재예측 엔드포인트
    Spring이 POST /api/intake-logs(음식 최종 확정) 처리 중 → 여기로 내부 호출

    Spring은 food_no/custom_food_no로 이미 알고 있는 영양성분(1 serving_size 기준)에
    사용자가 선택한 portion(0.5/1.0/1.5 등)을 곱해서 보내면, FastAPI가 LightGBM으로
    재예측하고 걷기 미션 목표(targetDistance/targetKcal)까지 계산해서 돌려준다.
    """
    if req.diagnosis_group not in DIAGNOSIS_GROUPS:
        return JSONResponse(
            content={"error": f"diagnosis_group은 {DIAGNOSIS_GROUPS} 중 하나여야 합니다."},
            status_code=400,
            media_type="application/json; charset=utf-8",
        )

    carb = req.carb * req.portion
    sugar = req.sugar * req.portion
    protein = req.protein * req.portion
    fat = req.fat * req.portion
    fiber = req.fiber * req.portion
    calorie = req.calorie * req.portion

    prediction = glucose_predictor.predict_peak(
        carb=carb, protein=protein, fat=fat, fiber=fiber,
        baseline=req.baseline, diagnosis_group=req.diagnosis_group,
    )
    mission = calc_walking_mission(prediction["predicted_peak"])

    return JSONResponse(
        content={
            "predictedGlucoseRise": prediction["predicted_rise"],
            "predictedPeak": prediction["predicted_peak"],
            "lowConfidence": prediction["low_confidence"],
            "targetDistance": mission["distance_km"],
            "targetKcal": mission["calories"],
            "nutritionUsed": {
                "carb": round(carb, 1),
                "sugar": round(sugar, 1),
                "protein": round(protein, 1),
                "fat": round(fat, 1),
                "fiber": round(fiber, 1),
                "calorie": round(calorie, 1),
            },
        },
        media_type="application/json; charset=utf-8",
    )
