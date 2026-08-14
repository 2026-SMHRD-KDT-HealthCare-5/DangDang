# -*- coding: utf-8 -*-
"""
POST /rag/intake-logs/reanalyze 라우터 — 사용자가 "틀려요, AI로 분석하기"를 눌렀을 때 Spring이 내부 호출한다.

목차
1. reanalyze_food() — image/food_name 필수값 검증 후 services/food_recognition.py의 reanalyze()에 위임
"""
from fastapi import APIRouter, Depends, File, Form, UploadFile
from fastapi.responses import JSONResponse

from core.security import verify_internal_api_key
from services.food_recognition import reanalyze

router = APIRouter(dependencies=[Depends(verify_internal_api_key)])


@router.post("/rag/intake-logs/reanalyze")
async def reanalyze_food(
    image: UploadFile | None = File(None),
    food_name: str | None = Form(None),
    baseline: float | None = Form(None),
    diagnosis_group: str | None = Form(None),
):
    """
    음식 AI 재분석 엔드포인트 (Spring 내부 호출용)

    사용자가 "틀려요, AI로 분석하기"를 선택했을 때만 호출됨.
    - image: 음식 사진 → Gemini Vision으로 분석
    - food_name: 음식명 텍스트 → Gemini 텍스트로 영양성분 추정
    둘 중 하나는 필수.

    ※ CUSTOM_FOOD 테이블 저장은 Spring 쪽에서 처리. FastAPI는 추정 결과만 반환한다.
    """
    if not image and not food_name:
        return JSONResponse(
            content={"error": "image 또는 food_name 중 하나는 필수입니다."},
            status_code=400,
            media_type="application/json; charset=utf-8",
        )

    status_code, content = await reanalyze(
        image=image, food_name=food_name, baseline=baseline, diagnosis_group=diagnosis_group
    )
    return JSONResponse(content=content, status_code=status_code, media_type="application/json; charset=utf-8")
