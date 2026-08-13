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
"""
import json
import re

from google.genai import types

from core.config import client, MODEL_NAME, log_token_usage, get_pre_glucose_default, DIAGNOSIS_GROUPS
from prompts.reanalyze import FOOD_REANALYSIS_IMAGE_PROMPT, FOOD_REANALYSIS_TEXT_PROMPT
from prompts.recognize import FOOD_RECOGNITION_PROMPT, TEXT_FOOD_EXTRACTION_PROMPT
from repositories.food_repo import food_db
from services.glucose_predictor import glucose_predictor

MATCH_SCORE_THRESHOLD = 70  # DB 유사도 매칭 최소 점수


def parse_gemini_json(text: str) -> dict:
    """Gemini가 ```json 코드블록으로 감싸서 응답하는 경우까지 안전하게 파싱"""
    cleaned = text.strip()
    cleaned = re.sub(r"^```json\s*|\s*```$", "", cleaned, flags=re.MULTILINE).strip()
    return json.loads(cleaned)


def _resolve_diag_and_baseline(diagnosis_group, baseline):
    diag = diagnosis_group if diagnosis_group in DIAGNOSIS_GROUPS else "건강군"
    bl = baseline if baseline is not None else get_pre_glucose_default(diag)
    return diag, bl


def _build_recognize_response(
    matched: bool,
    food_name: str,
    db_match: dict | None,
    baseline: float,
    diagnosis_group: str,
) -> dict:
    """음식 인식 결과를 명세서 형식의 응답 dict로 구성"""
    if matched and db_match:
        prediction = glucose_predictor.predict_peak(
            carb=float(db_match["carb"]),
            protein=float(db_match["protein"]),
            fat=float(db_match["fat"]),
            fiber=float(db_match["fiber"]),
            baseline=baseline,
            diagnosis_group=diagnosis_group,
        )
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
            "predictedGlucoseRise": prediction["predicted_rise"],
            "source": "공공데이터",
            "chatbotMessage": "식약처 데이터에서 찾았어요! 이 음식이 맞나요?",
        }
    # DD_101: 조회 실패 시 자동 AI 분석 수행하지 않음 — 안내만 표시
    return {
        "matched": False,
        "foodNo": None,
        "foodName": food_name,
        "serving_size": None,
        "nutrition": None,
        "predictedGlucoseRise": None,
        "source": None,
        "chatbotMessage": "식약처 데이터에서 찾지 못했어요. AI로 분석하거나 직접 입력해 주세요.",
    }


async def recognize(image, message, baseline, diagnosis_group) -> tuple[int, dict]:
    """
    - image: 음식 사진 (사진 인식 시)
    - message: 텍스트 입력 (채팅으로 음식명 입력 시)
    - baseline: 식전 혈당 (미입력 시 진단군별 기본값 적용)
    - diagnosis_group: 진단군 ("건강군" / "전당뇨" / "2형당뇨")
    """
    diag, bl = _resolve_diag_and_baseline(diagnosis_group, baseline)

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
        return 200, _build_recognize_response(False, food_name, None, bl, diag)

    # DB 매칭
    db_match = food_db.get_best_match(food_name, brand=brand)
    matched = db_match is not None and db_match["match_score"] >= MATCH_SCORE_THRESHOLD

    return 200, _build_recognize_response(matched, food_name, db_match if matched else None, bl, diag)


async def reanalyze(image, food_name, baseline, diagnosis_group) -> tuple[int, dict]:
    """
    사용자가 "틀려요, AI로 분석하기"를 선택했을 때만 호출됨.
    - image: 음식 사진 → Gemini Vision으로 분석
    - food_name: 음식명 텍스트 → Gemini 텍스트로 영양성분 추정
    둘 중 하나는 필수 (라우터에서 사전 검증).

    ※ CUSTOM_FOOD 테이블 저장은 Spring 쪽에서 처리한다. 여기서는 추정 결과만 반환한다.
    """
    diag, bl = _resolve_diag_and_baseline(diagnosis_group, baseline)

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

    prediction = glucose_predictor.predict_peak(
        carb=float(nutrition["carb"]),
        protein=float(nutrition["protein"]),
        fat=float(nutrition["fat"]),
        fiber=float(nutrition["fiber"]),
        baseline=bl,
        diagnosis_group=diag,
    )

    return 200, {
        "foodName": analysis.get("food_name", food_name or "알 수 없는 음식"),
        "serving_size": analysis.get("serving_size"),
        "nutrition": {
            "carb": nutrition.get("carb"),
            "sugar": nutrition.get("sugar"),
            "protein": nutrition.get("protein"),
            "fat": nutrition.get("fat"),
            "fiber": nutrition.get("fiber"),
            "calorie": nutrition.get("calorie"),
        },
        "predictedGlucoseRise": prediction["predicted_rise"],
        "source": "AI추정",
        "chatbotMessage": f"{source_msg} 정확하지 않을 수 있으니 확인해 주세요!",
    }
