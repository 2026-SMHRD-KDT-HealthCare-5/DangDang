# -*- coding: utf-8 -*-
"""
콜드스타트 혈당 예측 모델(final_risk_model.pkl) 연동 모듈

FEATURE_ORDER는 학습 노트북에서 확인된 실제 순서입니다:
    ['carbs_g', 'protein_g', 'fat_g', 'fiber_g', 'baseline',
     'group_2형당뇨', 'group_건강군', 'group_전당뇨']
(진단군 원-핫은 pandas get_dummies 알파벳/유니코드 정렬 순서라 '2형당뇨'가 맨 앞으로 옴)
"""

import pickle
import numpy as np

FEATURE_ORDER = [
    "탄수화물",
    "단백질",
    "지방",
    "식이섬유",
    "baseline",
    "진단군_2형당뇨",
    "진단군_건강군",
    "진단군_전당뇨",
]

DIAGNOSIS_GROUPS = ["건강군", "전당뇨", "2형당뇨"]

# 학습 데이터 baseline의 90th 백분위수. 이 값을 넘는 입력은 학습 데이터가
# 희박한 구간이라 예측이 불안정할 수 있음 (노트북에서 확인된 값)
MAX_RELIABLE_BASELINE = 168.8


class GlucosePredictor:
    def __init__(self, model_path: str = "final_risk_model.pkl"):
        with open(model_path, "rb") as f:
            self.model = pickle.load(f)

    def build_feature_vector(
        self,
        carb: float,
        protein: float,
        fat: float,
        fiber: float,
        baseline: float,
        diagnosis_group: str,
    ) -> np.ndarray:
        if diagnosis_group not in DIAGNOSIS_GROUPS:
            raise ValueError(
                f"diagnosis_group은 {DIAGNOSIS_GROUPS} 중 하나여야 합니다. 입력값: {diagnosis_group}"
            )

        one_hot = {f"진단군_{g}": (1 if g == diagnosis_group else 0) for g in DIAGNOSIS_GROUPS}

        values = {
            "탄수화물": carb,
            "단백질": protein,
            "지방": fat,
            "식이섬유": fiber,
            "baseline": baseline,
            **one_hot,
        }

        return np.array([[values[col] for col in FEATURE_ORDER]])

    def predict_peak(
        self,
        carb: float,
        protein: float,
        fat: float,
        fiber: float,
        baseline: float,
        diagnosis_group: str,
    ) -> dict:
        X = self.build_feature_vector(carb, protein, fat, fiber, baseline, diagnosis_group)
        peak = float(self.model.predict(X)[0])
        rise = peak - baseline

        return {
            "baseline": baseline,
            "predicted_peak": round(peak, 1),
            "predicted_rise": round(rise, 1),
            "low_confidence": baseline > MAX_RELIABLE_BASELINE,
        }


if __name__ == "__main__":
    predictor = GlucosePredictor("final_risk_model.pkl")
    result = predictor.predict_peak(
        carb=15.23, protein=20.85, fat=16.72, fiber=1.21,
        baseline=110, diagnosis_group="전당뇨",
    )
    print(result)
