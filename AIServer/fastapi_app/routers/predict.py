# -*- coding: utf-8 -*-
"""
POST /rag/intake-logs/predict 라우터 — 음식을 최종 확정(portion 반영)할 때 Spring이 내부 호출한다.

목차
1. predict_with_portion() — diagnosis_group 검증 → portion 배율 적용 → 혈당 재예측 → 걷기 미션 계산까지 한 번에 처리
"""
from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from core.config import DIAGNOSIS_GROUPS
from core.security import verify_internal_api_key
from schemas.predict import PortionPredictRequest
from services.glucose_predictor import glucose_predictor
from services.mission_calculator import calc_walking_mission

router = APIRouter(dependencies=[Depends(verify_internal_api_key)])


@router.post("/rag/intake-logs/predict")
def predict_with_portion(req: PortionPredictRequest):
    """
    음식 portion 반영 재예측 엔드포인트
    Spring이 POST /api/intake-logs(음식 최종 확정) 처리 중 → 여기로 내부 호출

    Spring은 food_no/custom_food_no로 이미 알고 있는 영양성분(1 serving_size 기준)에
    사용자가 선택한 portion(0.5/1.0/1.5 등)을 곱해서 보내면, FastAPI가 LightGBM으로
    재예측하고 걷기 미션 목표(targetDistance/targetKcal/targetTimeMinutes)까지 계산해서 돌려준다.

    [추가] targetTimeMinutes는 calc_walking_mission()이 애초에 targetDistance를 만들 때
    쓴 "몇 분 걸어야 하는지"(walk_minutes) 값을 그대로 노출한 것입니다. targetDistance로부터
    별도 페이스 공식(예: distance*12)으로 재계산하지 않습니다 — 이미 있는 원본값을 다시
    근사해서 만들면 이중 계산이고 오차만 더해질 뿐이라, 계산의 출처(walk_minutes)를
    그대로 돌려주는 게 더 정확합니다.
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
            "targetDistance": mission["distance_m"],
            "targetKcal": mission["calories"],
            "targetTimeMinutes": mission["walk_minutes"],
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
