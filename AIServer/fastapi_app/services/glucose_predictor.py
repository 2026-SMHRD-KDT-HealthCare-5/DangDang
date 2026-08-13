# -*- coding: utf-8 -*-
"""
콜드스타트 혈당 예측 모델(final_risk_model.pkl) 연동 모듈

FEATURE_ORDER는 학습 노트북에서 확인된 실제 순서입니다:
    ['carbs_g', 'protein_g', 'fat_g', 'fiber_g', 'baseline',
     'group_2형당뇨', 'group_건강군', 'group_전당뇨']
(진단군 원-핫은 pandas get_dummies 알파벳/유니코드 정렬 순서라 '2형당뇨'가 맨 앞으로 옴)
"""

import pickle
from pathlib import Path

import numpy as np

from core.config import DIAGNOSIS_GROUPS

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

# 학습 데이터 baseline의 90th 백분위수. 이 값을 넘는 입력은 학습 데이터가
# 희박한 구간이라 예측이 불안정할 수 있음 (노트북에서 확인된 값)
MAX_RELIABLE_BASELINE = 168.8

# fastapi_app/models/final_risk_model.pkl 고정 위치.
# __file__ 기준 상대경로라 uvicorn을 어느 위치에서 실행해도 항상 같은 파일을 찾는다.
DEFAULT_MODEL_PATH = Path(__file__).resolve().parent.parent / "models" / "final_risk_model.pkl"


class GlucosePredictor:
    def __init__(self, model_path: Path | str = DEFAULT_MODEL_PATH):
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


# 모듈 최초 import 시 1회만 로드되어 이후 재사용된다 (Python import 캐싱).
# NFR-DV-001(예측 15초 이내 응답)을 만족하려면 요청마다 모델을 새로 로드하면 안 되는데,
# 여기서 모듈 레벨 싱글턴으로 만들어두면 그 요구사항이 자연스럽게 지켜진다.
# → models/final_risk_model.pkl 이 로컬에 없으면 이 줄에서 바로 FileNotFoundError가 난다.
#   (.gitignore로 제외된 파일이라 각자 로컬에 직접 받아둬야 함)
glucose_predictor = GlucosePredictor()


if __name__ == "__main__":
    result = glucose_predictor.predict_peak(
        carb=15.23, protein=20.85, fat=16.72, fiber=1.21,
        baseline=110, diagnosis_group="전당뇨",
    )
    print(result)
