# models/

`final_risk_model.pkl` (콜드스타트 혈당 예측 LightGBM 모델)을 이 폴더에 넣어야
`services/glucose_predictor.py`가 정상적으로 로드됩니다.

`*.pkl`은 루트 `.gitignore`에서 제외되어 있습니다 (GitHub 100MB 제한 + 팀원마다
다른 학습 버전을 쓸 수 있어서). 그래서 이 폴더는 git에는 빈 채로 존재하고,
`final_risk_model.pkl`은 각자 로컬에서 직접 받아서/학습해서 넣어야 합니다.

파일이 없는 상태로 서버를 켜면 `services/glucose_predictor.py`의
`glucose_predictor = GlucosePredictor()` 줄에서 `FileNotFoundError`가 납니다.
