# [각주 1] FastAPI(AI 서버)용 Dockerfile. Spring과 달리 컴파일 과정이 없는 파이썬이라
# 멀티스테이지로 나눌 필요 없이 한 단계로 충분합니다.
FROM python:3.11-slim
WORKDIR /app

# [각주 2] libgomp1 : lightgbm(예측 모델 라이브러리)이 내부적으로 병렬연산(OpenMP)에 쓰는
# 시스템 라이브러리입니다. python:3.11-slim에는 기본으로 안 들어있어서 따로 설치해야
# lightgbm import 시점에 "libgomp.so.1 not found" 같은 에러가 안 납니다.
RUN apt-get update && apt-get install -y --no-install-recommends libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# requirements.txt만 먼저 복사해서 의존성 설치 레이어를 캐싱합니다
# (코드만 바뀌고 requirements.txt가 안 바뀌면, pip install을 매번 새로 안 해도 됨)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# [각주 3] data/food_for_db.csv, db_import/*.sql 등도 이 COPY . . 한 줄에 같이 들어갑니다.
# food_for_db.csv는 DB 연결 실패 시의 폴백용이라 이미지에 들어있어도 무방합니다
# (평소엔 DB 모드로 동작하므로 안 쓰임 — 앞서 진단한 "롯데리아 DB 미적재" 이슈와 별개입니다).

EXPOSE 8000

# [각주 4] --host 0.0.0.0 이 중요합니다. 기본값(127.0.0.1)으로 두면 컨테이너 "안"에서만
# 접속 가능해서, 같은 docker-compose 네트워크의 backend 컨테이너조차 못 들어옵니다.
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
