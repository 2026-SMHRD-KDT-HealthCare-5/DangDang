# -*- coding: utf-8 -*-
"""
예측 peak 혈당(PPG, mg/dL) 기준 걷기 미션(시간/거리/칼로리) 환산

목차
1. calc_walking_mission() — PPG를 걷기 시간(분)/거리(m)/칼로리로 변환하는 유일한 함수

[수정] 거리 단위를 km에서 m(미터)로 바꿨습니다. Spring 쪽(WalkMissionService)의 체크포인트
최소 이동거리(1m)/이상치 속도(초당 4.5m) 판정이 currentDistance를 "미터" 단위로 가정하고
있었는데, 이 함수가 km으로 내려주고 있어서 단위가 안 맞았던 걸 여기서 맞췄습니다.
"""


def calc_walking_mission(ppg: float) -> dict:
    """
    예측 peak 혈당(PPG, mg/dL) 기준 3구간 걷기 시간 공식

    PPG ≤ 140          : T = 10 (항상 최소 10분)
    140 < PPG < 200     : T = 10 + (PPG - 140) / 60 * 20   (10~30분)
    PPG ≥ 200           : T = 30 + min((PPG - 200) / 50, 1) * 15   (30~45분 상한)
    """
    if ppg <= 140:
        minutes = 10
    elif ppg < 200:
        minutes = 10 + (ppg - 140) / 60 * 20
    else:
        minutes = 30 + min((ppg - 200) / 50, 1) * 15

    minutes = round(minutes)
    distance_m = round(minutes * 60)  # 분당 약 60m 도보 가정 (km 환산 없이 그대로 미터)
    calories = round(minutes * 4)  # 분당 약 4kcal 소모 가정

    return {"walk_minutes": minutes, "distance_m": distance_m, "calories": calories}
