# -*- coding: utf-8 -*-
from fastapi import APIRouter, File, Form, UploadFile
from fastapi.responses import JSONResponse

from services.food_recognition import recognize

router = APIRouter()


@router.post("/rag/intake-logs/recognize")
async def recognize_food(
    image: UploadFile | None = File(None),
    message: str | None = Form(None),
    baseline: float | None = Form(None),
    diagnosis_group: str | None = Form(None),
):
    """
    음식 인식 엔드포인트 (Spring 내부 호출용)

    - image: 음식 사진 (사진 인식 시)
    - message: 텍스트 입력 (채팅으로 음식명 입력 시)
    - baseline: 식전 혈당 (미입력 시 진단군별 기본값 적용)
    - diagnosis_group: 진단군 ("건강군" / "전당뇨" / "2형당뇨")
    """
    # 사진도 텍스트도 없으면 에러
    if not image and not message:
        return JSONResponse(
            content={"error": "image 또는 message 중 하나는 필수입니다."},
            status_code=400,
            media_type="application/json; charset=utf-8",
        )

    status_code, content = await recognize(
        image=image, message=message, baseline=baseline, diagnosis_group=diagnosis_group
    )
    return JSONResponse(content=content, status_code=status_code, media_type="application/json; charset=utf-8")
