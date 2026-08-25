# -*- coding: utf-8 -*-
"""
음식 인식(사진/텍스트 → 음식명 후보 추출 → DB 매칭) + AI 재분석 서비스.

recognize()  : 2단계 파이프라인
               1) Gemini로 사진/텍스트에서 음식명 후보를 인식
               2) repositories.food_repo에서 텍스트 유사도가 가장 높은 항목 1건을 매칭
reanalyze()  : 사용자가 "틀려요, AI로 분석하기"를 선택했을 때, Gemini가 영양성분까지 직접 추정

두 함수 모두 (status_code, content_dict)를 반환한다. 라우터는 이 값을 그대로
JSONResponse(status_code=status_code, content=content)로 감싸기만 하면 된다 —
비즈니스 로직과 HTTP 응답 구성을 분리해서, 라우터는 얇게 유지한다.

목차
1. parse_gemini_json() — Gemini가 ```json 코드블록으로 감싼 응답까지 안전하게 파싱
2. _build_recognize_response() — 인식 결과를 최종 응답 형태로 조립
3. recognize() — routers/recognize.py가 호출하는 진입점 (음식 인식 2단계 파이프라인)
4. reanalyze() — routers/reanalyze.py가 호출하는 진입점 (AI 재분석)

[수정] predictedGlucoseRise를 recognize/reanalyze 단계에서 미리 계산하던 로직을 없앴습니다.
이 시점엔 portion(몇 인분)을 몰라서 항상 1인분 가정으로만 계산됐고, 어차피 /predict가
실제 portion으로 다시 계산하는 값이라 예측 모델 호출이 낭비였습니다. 그래서 이 계산에만
쓰이던 _resolve_diag_and_baseline() 헬퍼와 glucose_predictor import도 같이 뺐습니다.
"""
import json
import re

from google.genai import types

from core.config import client, MODEL_NAME, log_token_usage
from prompts.reanalyze import FOOD_REANALYSIS_IMAGE_PROMPT, FOOD_REANALYSIS_TEXT_PROMPT
from prompts.recognize import FOOD_RECOGNITION_PROMPT, TEXT_FOOD_EXTRACTION_PROMPT
from repositories.food_repo import food_db

MATCH_SCORE_THRESHOLD = 70  # DB 유사도 매칭 최소 점수


def parse_gemini_json(text: str) -> dict:
    """Gemini가 ```json 코드블록으로 감싸서 응답하는 경우까지 안전하게 파싱"""
    cleaned = text.strip()
    cleaned = re.sub(r"^```json\s*|\s*```$", "", cleaned, flags=re.MULTILINE).strip()
    return json.loads(cleaned)


def _build_recognize_response(
    matched: bool,
    food_name: str,
    db_match: dict | None,
) -> dict:
    """음식 인식 결과를 명세서 형식의 응답 dict로 구성.

    [수정] predictedGlucoseRise는 여기서 계산하지 않습니다 — 이 시점엔 portion(몇 인분
    먹었는지)을 아직 모르기 때문에 항상 1인분 가정으로 계산될 수밖에 없고, 어차피
    /predict가 실제 portion을 반영해서 다시 계산합니다. 화면도 recognize 응답 시점엔
    "얼마나 드셨어요?"만 물어보고 카드/예측치는 predict 응답을 받은 뒤에 보여주므로,
    이 값을 미리 계산하는 건 예측 모델 호출만 낭비하는 것이었습니다.
    baseline/diagnosis_group 인자도 이 계산에만 쓰였어서 같이 뺐습니다.
    """
    if matched and db_match:
        return {
            "matched": True,
            "foodNo": db_match["food_no"],
            "foodName": db_match["food_name"],
            "serving_size": db_match["serving_size"],
            "nutrition": {
                "carb": db_match["carb"],
                "sugar": db_match["sugar"],
                "protein": db_match["protein"],
                "fat": db_match["fat"],
                "fiber": db_match["fiber"],
                "calorie": db_match["calorie"],
            },
            "source": "공공데이터",
            "chatbotMessage": "식약처 데이터에서 찾았어요! 이 음식이 맞나요?",
        }
    # DD_101: 조회 실패 시 자동 AI 분석 수행하지 않음 — 안내만 표시
    # [각주] (수정 2026-08-24) 예전엔 foodName에 Gemini가 추정한 이름(food_name)을 그대로
    # 돌려줬는데, 이러면 화면에 "검증 안 된 이름"이 마치 매칭된 것처럼 보여서 혼란을 줬습니다
    # (사용자 결정 — 화면 시안: "검색 결과가 없어요"만 보여주고 이름 자체를 안 보여줌).
    # 그래서 매칭 실패 시엔 foodName도 null로 보냅니다. food_name 값 자체는 로그로만 남깁니다.
    return {
        "matched": False,
        "foodNo": None,
        "foodName": None,
        "serving_size": None,
        "nutrition": None,
        "source": None,
        "chatbotMessage": "식약처 데이터에서 찾지 못했어요. AI로 분석하거나 직접 입력해 주세요.",
    }


async def recognize(image, message, baseline, diagnosis_group) -> tuple[int, dict]:
    """
    - image: 음식 사진 (사진 인식 시)
    - message: 텍스트 입력 (채팅으로 음식명 입력 시)
    - baseline, diagnosis_group: [수정] 더 이상 여기서 안 씁니다 — predictedGlucoseRise
      계산을 뺀 뒤로 이 함수 안에서 쓸 곳이 없어졌습니다. 라우터/Spring이 여전히 이
      값들을 보내더라도 그냥 받기만 하고 무시합니다(호출부 깨지지 않게 시그니처는 유지).
    """
    # --- 사진 입력 ---
    if image:
        image_bytes = await image.read()
        response = client.models.generate_content(
            model=MODEL_NAME,
            contents=[
                types.Part.from_bytes(data=image_bytes, mime_type=image.content_type),
                FOOD_RECOGNITION_PROMPT,
            ],
        )
        log_token_usage(response, label="recognize-image")

        try:
            recognition = parse_gemini_json(response.text)
        except Exception:
            return 500, {"error": "인식 결과 파싱 실패", "raw": response.text}

        food_name = recognition.get("food_name", "")
        brand = recognition.get("brand")

    # --- 텍스트 입력 ---
    else:
        response = client.models.generate_content(
            model=MODEL_NAME,
            contents=message,
            config={"system_instruction": TEXT_FOOD_EXTRACTION_PROMPT, "temperature": 0.0},
        )
        log_token_usage(response, label="recognize-text")

        try:
            extraction = parse_gemini_json(response.text)
        except Exception:
            # 파싱 실패 시 입력 텍스트를 음식명으로 직접 사용
            extraction = {"food_name": message.strip(), "brand": None}

        food_name = extraction.get("food_name", message.strip())
        brand = extraction.get("brand")

    # 인식 불가
    if food_name == "인식불가":
        return 200, _build_recognize_response(False, food_name, None)

    # DB 매칭
    db_match = food_db.get_best_match(food_name, brand=brand)
    matched = db_match is not None and db_match["match_score"] >= MATCH_SCORE_THRESHOLD

    # [각주] (추가 2026-08-24) 매칭 실패 시 응답의 foodName은 null로 나가서(위 [수정] 참고)
    # Gemini가 뭐라고 추정했었는지가 응답에서는 안 보입니다. 디버깅/DB 보강 시 참고할 수 있게
    # 서버 로그에만 남겨둡니다 (사용자에게는 노출 안 됨).
    if not matched:
        print(f"[food_recognition] 매칭 실패 — Gemini 추정명: '{food_name}' (brand={brand})")

    return 200, _build_recognize_response(matched, food_name, db_match if matched else None)


async def reanalyze(image, food_name, baseline, diagnosis_group) -> tuple[int, dict]:
    """
    사용자가 "틀려요, AI로 분석하기"를 선택했을 때만 호출됨.
    - image: 음식 사진 → Gemini Vision으로 분석
    - food_name: 음식명 텍스트 → Gemini 텍스트로 영양성분 추정
    둘 중 하나는 필수 (라우터에서 사전 검증).
    - baseline, diagnosis_group: [수정] predictedGlucoseRise 계산을 뺀 뒤로 여기서 안 씁니다
      (recognize()와 동일한 이유 — portion을 모르는 시점이라 계산해봐야 /predict에서 다시
      계산될 값이라 예측 모델 호출만 낭비였습니다).

    ※ CUSTOM_FOOD 테이블 저장은 Spring 쪽에서 처리한다. 여기서는 추정 결과만 반환한다.
    """
    # --- 사진 분석 ---
    if image:
        image_bytes = await image.read()
        response = client.models.generate_content(
            model=MODEL_NAME,
            contents=[
                types.Part.from_bytes(data=image_bytes, mime_type=image.content_type),
                FOOD_REANALYSIS_IMAGE_PROMPT,
            ],
        )
        log_token_usage(response, label="reanalyze-image")
        source_msg = "AI가 사진을 분석해서 영양성분을 추정했어요."
    # --- 텍스트 분석 ---
    else:
        response = client.models.generate_content(
            model=MODEL_NAME,
            contents=f"음식: {food_name}",
            config={"system_instruction": FOOD_REANALYSIS_TEXT_PROMPT, "temperature": 0.2},
        )
        log_token_usage(response, label="reanalyze-text")
        source_msg = f"AI가 '{food_name}'의 영양성분을 추정했어요."

    try:
        analysis = parse_gemini_json(response.text)
    except Exception:
        return 500, {"error": "AI 분석 결과 파싱 실패", "raw": response.text}

    nutrition = analysis.get("nutrition", {})
    required_keys = ["carb", "protein", "fat", "fiber"]

    if not all(k in nutrition for k in required_keys):
        return 500, {"error": "AI가 영양성분을 추정하지 못했습니다.", "raw": analysis}

    # 텍스트로 입력한 경우(food_name이 있음)는 사용자가 준 이름을 그대로 신뢰합니다.
    # Gemini가 못 알아듣고 엉뚱한 진짜 음식 이름으로 바꿔치기(할루시네이션)하는 걸 막기 위함입니다.
    # 사진만 준 경우(food_name 없음)는 Gemini가 음식 자체를 식별해야 하니 그대로 둡니다.
    resolved_food_name = food_name if food_name else analysis.get("food_name", "알 수 없는 음식")

    return 200, {
        "foodName": resolved_food_name,
        "serving_size": analysis.get("serving_size"),
        "nutrition": {
            "carb": nutrition.get("carb"),
            "sugar": nutrition.get("sugar"),
            "protein": nutrition.get("protein"),
            "fat": nutrition.get("fat"),
            "fiber": nutrition.get("fiber"),
            "calorie": nutrition.get("calorie"),
        },
        "source": "AI추정",
        "chatbotMessage": f"{source_msg} 정확하지 않을 수 있으니 확인해 주세요!",
    }
