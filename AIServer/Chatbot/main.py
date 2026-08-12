"""
당당이 챗봇 - 1단계 프로토타입 (Gemini API 버전)
벡터DB 없이 고정 지식(fixed knowledge)을 시스템 프롬프트에 넣어
Gemini API로 대화형 응답을 생성하는 최소 기능 버전.

* GL(혈당부하지수) 계산은 이 서비스 범위에서 제외 — 걷기/식후 관리 조언 중심으로만 구성

실행:
    pip install fastapi uvicorn google-genai python-dotenv
    export GEMINI_API_KEY=발급받은키
    uvicorn main:app --reload
"""

import os
from dotenv import load_dotenv
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from google import genai
from google.genai import types
from nutrition_db.food_lookup import FoodDB
from glucose_model import GlucosePredictor, DIAGNOSIS_GROUPS
from rag.paper_qa import create_paper_cache, answer_with_paper_cache, answer_without_cache, load_combined_text

load_dotenv()  # 같은 폴더의 .env 파일을 읽어서 환경변수로 등록

app = FastAPI(title="당당이 챗봇 프로토타입 (1단계, Gemini)")
client = genai.Client(api_key=os.environ.get("GEMINI_API_KEY"))

# gemini-2.5-flash-lite는 신규 사용자에게 더 이상 제공되지 않음.
# gemini-3.1-flash-lite로 대체 (저렴하면서 최신 모델)
MODEL_NAME = "gemini-3.1-flash-lite"

# DB 스키마와 통일된 최종 영양성분 데이터 로드 (브랜드는 food_name에 병합되어 있음)
food_db = FoodDB("nutrition_db/db_import/food_for_db.csv")

# 콜드스타트 혈당 예측 모델 로드
glucose_predictor = GlucosePredictor("final_risk_model.pkl")

# 논문들을 텍스트로 추출해서 하나로 합친 파일을 로드 (없으면 자동 생성 시도)
combined_papers_text = load_combined_text()

# 합쳐진 텍스트를 컨텍스트 캐시로 등록 (매 질문마다 논문 재처리 안 해도 되게)
# 실패하면 paper_cache가 None이 되고, 그때는 매번 텍스트 전체를 프롬프트에 넣는 방식으로 폴백됨
paper_cache = create_paper_cache(client, MODEL_NAME)


def log_token_usage(response, label: str = ""):
    """Gemini 응답의 토큰 사용량을 터미널에 출력"""
    usage = getattr(response, "usage_metadata", None)
    if usage is None:
        print(f"[토큰 사용량{f' - {label}' if label else ''}] 정보 없음")
        return

    prompt_tokens = usage.prompt_token_count or 0
    output_tokens = usage.candidates_token_count or 0
    total_tokens = usage.total_token_count or 0

    print(
        f"[토큰 사용량{f' - {label}' if label else ''}] "
        f"입력: {prompt_tokens} / 출력: {output_tokens} / 합계: {total_tokens}"
    )


# ---------------------------------------------------------
# 사용자별 대화 세션 (멀티턴 메모리)
# 주의: 서버 메모리에만 저장되므로 서버 재시작하면 초기화됨.
#       추후 DB/Redis 등으로 영속화 필요.
# ---------------------------------------------------------
chat_sessions: dict[str, "genai.chats.Chat"] = {}


def get_or_create_chat_session(user_id: str, system_prompt: str):
    if user_id not in chat_sessions:
        chat_sessions[user_id] = client.chats.create(
            model=MODEL_NAME,
            config={
                "system_instruction": system_prompt,
                "temperature": 0.7,
            },
        )
    return chat_sessions[user_id]

# ---------------------------------------------------------
# 1. 고정 지식베이스 (2단계에서 이 부분이 벡터DB 검색으로 대체됨)
#    GL 계산은 하지 않음 — 정성적인 식후 관리 지식만 포함
# ---------------------------------------------------------
FIXED_KNOWLEDGE = """
[혈당관리 참고 지식]
- 식후 혈당 상승은 보통 식사 후 30분~1시간 사이에 시작되어 1~2시간 내 정점에 도달
- 식후 가벼운 걷기(15~30분)는 혈당 상승 폭을 낮추는 데 도움이 됨
- 탄수화물 위주 식사는 단백질/지방을 곁들이면 혈당 상승 속도가 완만해짐
- 흰쌀밥, 면류, 빵류처럼 정제 탄수화물 비중이 높은 식사일수록 식후 걷기가 더 도움이 됨
- 2030세대는 당뇨 전단계 인지율이 낮아 조기 관리가 중요함
"""

# ---------------------------------------------------------
# 2. 사용자 컨텍스트 (지금은 더미 데이터, 추후 DB 연동)
# ---------------------------------------------------------
def get_dummy_user_context(user_id: str) -> str:
    return f"""
[사용자 정보: {user_id}]
- 최근 식사: 흰쌀밥 + 제육볶음
- 오늘 걷기 기록: 아직 없음
- 최근 3일 평균 걷기: 18분
"""

# ---------------------------------------------------------
# 3. 당당이 페르소나 시스템 프롬프트
# ---------------------------------------------------------
SYSTEM_PROMPT_TEMPLATE = """너는 '당당이'라는 AI 건강비서야.
'당당' 서비스의 혈당관리 헬스케어 챗봇으로, 사용자의 식후 혈당 관리를 돕는 역할이야.

[말투/태도]
- 친근하고 공감형 어투를 사용해 (반말은 하지 말고 다정한 존댓말)
- 걱정을 유발하지 않고, 응원하듯 걷기를 유도해
- 답변은 3~5문장 이내로 간결하게

[답변 원칙]
- 아래 참고 지식과 사용자 정보를 바탕으로 답변해
- GL(혈당부하지수) 같은 수치를 계산하거나 단정적으로 제시하지 마 — 이 서비스는 GL을 계산하지 않음
- 확실하지 않은 의학적 진단은 하지 말고, 필요시 전문의 상담을 권유해
- 가능하면 답변 끝에 자연스럽게 걷기 미션을 한 줄 제안해
- 인슐린 용량이나 복용 중인 약의 종류·용량에 대해서는 절대 조언하지 마. 이런 질문이 나오면
  "이 부분은 담당 의사·약사와 상담해주세요"라고 안내하고, 구체적인 수치나 판단은 절대 제시하지 마

{knowledge}
{user_context}
"""

# ---------------------------------------------------------
# 4. 요청/응답 스키마
# ---------------------------------------------------------
class ChatRequest(BaseModel):
    user_id: str
    message: str
    diagnosis_group: str | None = None  # 넘어오면 해당 유저의 진단군으로 저장


class ChatResponse(BaseModel):
    reply: str


# ---------------------------------------------------------
# 사용자별 진단군 저장 (메모리, 서버 재시작 시 초기화 — 추후 DB로 영속화)
# 진단군을 아직 모르면 기본값 "건강군"으로 처리
# ---------------------------------------------------------
user_diagnosis_groups: dict[str, str] = {}


# 식전 혈당 미입력 시 진단군별 기본값 (mg/dL)
# ADA/대한당뇨병학회 공복혈당 범위의 중간값 기준
#   건강군: 70~99  → 95
#   전당뇨: 100~125 → 115
#   2형당뇨: 126~168 → 140 (모델 신뢰구간 MAX_RELIABLE_BASELINE=168.8 이내)
PRE_GLUCOSE_DEFAULTS = {
    "건강군": 95,
    "전당뇨": 115,
    "2형당뇨": 140,
}


def get_pre_glucose_default(diagnosis_group: str) -> int:
    """식전 혈당 미입력 시 진단군별 기본값 반환"""
    return PRE_GLUCOSE_DEFAULTS.get(diagnosis_group, 95)


# ---------------------------------------------------------
# 약물/인슐린 용량 관련 질문 차단 (안전상 절대 답변하지 않음)
# LLM 판단에 맡기지 않고 키워드로 확실하게 걸러서 고정 문구로 응답
# ---------------------------------------------------------
MEDICATION_SAFETY_MESSAGE = (
    "죄송하지만 인슐린 용량이나 복용 중인 약의 종류·용량에 대해서는 "
    "제가 안내해드릴 수 없어요. 이런 부분은 반드시 담당 의사·약사와 "
    "상담해서 결정하셔야 하는 부분이에요. 다른 궁금하신 점은 편하게 물어봐주세요!"
)

# "인슐린"이 언급되면 무조건 차단 (단위/용량 조정은 특히 위험도가 높음)
INSULIN_KEYWORDS = ["인슐린", "insulin"]

# 국내에서 흔히 쓰이는 인슐린/당뇨약 브랜드명·성분명 (영문 표기, 흔한 오타 포함).
# 이 자체가 언급되면 "용량" 같은 단어가 없어도 약물 관련 질문일 가능성이 높아 차단 대상에 포함.
DRUG_BRAND_KEYWORDS = [
    # 인슐린 제제 (한글 + 영문 + 흔한 오타)
    "휴마로그", "후마로그", "humalog",
    "휴물린", "후물린", "humulin",
    "노보래피드", "novorapid",
    "노보믹스", "novomix",
    "란투스", "lantus",
    "레버미어", "levemir",
    "트레시바", "tresiba",
    "애피드라", "아피드라", "apidra",
    "인슐라틴",
    "투제오", "toujeo",
    # 경구용 혈당강하제 (한글 + 영문)
    "메트포르민", "metformin",
    "다이아벡스", "글루코파지", "glucophage",
    "자디앙", "jardiance",
    "포시가", "forxiga",
    "슈글렛",
    "트라젠타", "trajenta",
    "자누비아", "januvia",
    "아마릴", "amaryl",
    "글리메피리드", "glimepiride",
    "다파글리플로진", "dapagliflozin",
    "엠파글리플로진", "empagliflozin",
    "시타글립틴", "sitagliptin",
    "리벨서스", "rybelsus",
    "오젬픽", "ozempic",
    "마운자로", "mounjaro",
    # 동반질환 약 (당뇨약과 상호작용/병용 질문도 같은 리스크군)
    "혈압약", "고지혈증약", "콜레스테롤약",
]

# 약 종류/용량/복용법을 직접 가리키는 키워드 (이것만으로도 바로 차단)
MEDICATION_DOSAGE_KEYWORDS = [
    "용량", "복용량", "복용법",
    "몇 알", "몇알", "몇 정", "몇정", "몇 개",
    "몇 mg", "몇mg", "mg", "밀리그램",
    "몇 단위", "몇단위", "단위",  # 인슐린 단위(U/IU) 관련 질문 포괄
    "iu", "cc",
    "약 종류", "무슨 약", "어떤 약", "약 이름",
    "경구제", "주사제",
    # 과다 복용 표현은 "약"이라는 단어 없이도 그 자체로 의미가 명확해서 바로 차단
    "두 배로 먹", "두배로 먹", "곱절로 먹",
]

# 복용량 조정/실수를 나타내는 동사 어간. "약"이나 약물명과 문장 어디서든
# 같이 등장하면 차단 (반드시 붙어있지 않아도 됨 — "약 좀 줄이고" 같은 경우 대비).
# 한국어는 "줄이다/늘리다/바꾸다" 같은 모음 어간 동사가 'ㄹ까/ㄹ게' 등과 결합하면
# "줄일까요", "늘릴게요"처럼 축약되어 원래 어간이 그대로 안 남기 때문에
# 축약된 형태도 같이 넣어둠.
DOSAGE_ADJUSTMENT_STEMS = [
    "줄이", "줄일",   # 줄이고/줄이면 vs 줄일까요/줄일게요
    "늘리", "늘릴",   # 늘리고/늘리면 vs 늘릴까요/늘릴게요
    "바꾸", "바꿀",   # 바꾸고/바꾸면 vs 바꿀까요/바꿀게요
    "끊",             # 끊고/끊으면/끊을까요 (자음 어간이라 축약 없음)
    "깜빡", "빼먹",    # 복용 실수(누락)
]

# 멀티턴에서 "이 약", "그 약"처럼 간접적으로 가리키는 경우도 최대한 커버
INDIRECT_DRUG_REFERENCE = ["이 약", "그 약", "저 약"]


def is_medication_dosage_question(message: str) -> bool:
    text = message.lower()  # 영문 브랜드명/단위 대소문자 무시하고 매칭

    if any(kw.lower() in text for kw in INSULIN_KEYWORDS):
        return True
    if any(kw.lower() in text for kw in DRUG_BRAND_KEYWORDS):
        return True
    if any(kw.lower() in text for kw in MEDICATION_DOSAGE_KEYWORDS):
        return True
    if any(kw in message for kw in INDIRECT_DRUG_REFERENCE):
        return True

    # "약"이라는 단어(또는 인슐린/브랜드명)와 조정 동사가 문장 안에 같이 있으면 차단
    # (반드시 붙어 있을 필요 없음: "약 좀 줄이고 싶은데" 같은 경우도 잡기 위함)
    has_drug_mention = (
        "약" in message
        or any(kw.lower() in text for kw in INSULIN_KEYWORDS)
        or any(kw.lower() in text for kw in DRUG_BRAND_KEYWORDS)
    )
    has_adjustment_verb = any(stem in message for stem in DOSAGE_ADJUSTMENT_STEMS)
    if has_drug_mention and has_adjustment_verb:
        return True

    return False


@app.post("/rag/chat")
def chat(req: ChatRequest):
    """
    일반 대화 엔드포인트 (Spring이 /api/chat -> 여기로 내부 호출)

    음식 인식/혈당 예측은 이 엔드포인트의 역할이 아님 — 그건
    /rag/intake-logs/recognize, /rag/intake-logs/reanalyze,
    /rag/intake-logs/predict가 전담한다. 여긴 순수 대화(페르소나 응답,
    논문 기반 Q&A)만 처리한다.
    """
    # 인슐린/약물 용량 관련 질문은 Gemini 호출 전에 키워드로 먼저 차단
    if is_medication_dosage_question(req.message):
        return JSONResponse(
            content={"reply": MEDICATION_SAFETY_MESSAGE},
            media_type="application/json; charset=utf-8",
        )

    if req.diagnosis_group:
        user_diagnosis_groups[req.user_id] = req.diagnosis_group

    # 논문 기반 지식 질문 -> 논문 텍스트 컨텍스트로 답변
    if paper_cache:
        # 캐시가 있으면 논문을 매번 다시 안 보내고 캐시만 참조 (훨씬 빠름)
        response = answer_with_paper_cache(client, MODEL_NAME, req.message, paper_cache)
        log_token_usage(response, label="chat-paper-cached")

        return JSONResponse(
            content={"reply": response.text},
            media_type="application/json; charset=utf-8",
        )
    elif combined_papers_text:
        # 캐시 생성이 실패했을 때의 폴백 (느리지만 동작은 함)
        response = answer_without_cache(client, MODEL_NAME, req.message, combined_papers_text)
        log_token_usage(response, label="chat-paper-nocache")

        return JSONResponse(
            content={"reply": response.text},
            media_type="application/json; charset=utf-8",
        )

    # 논문 리소스가 아예 없을 때의 최종 폴백 -> 페르소나 대화 세션
    user_context = get_dummy_user_context(req.user_id)
    system_prompt = SYSTEM_PROMPT_TEMPLATE.format(
        knowledge=FIXED_KNOWLEDGE,
        user_context=user_context,
    )
    chat_session = get_or_create_chat_session(req.user_id, system_prompt)
    response = chat_session.send_message(req.message)

    log_token_usage(response, label="chat")

    # PowerShell(Windows) 등 일부 클라이언트가 charset 없는 응답을
    # 잘못 해석해 한글이 깨지는 문제 방지 위해 charset 명시
    return JSONResponse(
        content={"reply": response.text},
        media_type="application/json; charset=utf-8",
    )


@app.get("/")
def health_check():
    return JSONResponse(
        content={"status": "ok", "service": "당당이 챗봇 프로토타입 (1단계, Gemini, RAG 연동)"},
        media_type="application/json; charset=utf-8",
    )


# ---------------------------------------------------------
# 6. 음식 인식 + 영양성분 DB 매칭 엔드포인트
#    Spring이 /api/intake-logs/recognize → 여기로 내부 호출
#    입력: 사진(image) 또는 텍스트(message) — 둘 중 하나 필수
# ---------------------------------------------------------
FOOD_RECOGNITION_PROMPT = """이 사진 속 음식을 인식해서 아래 JSON 형식으로만 답해.
설명 문장이나 마크다운 코드블록(```json) 없이, 순수 JSON 텍스트만 출력해.

{
  "food_name": "음식 이름 (한글, 검색하기 좋게 간결하게. 예: '황금올리브 치킨', '불고기 피자')",
  "brand": "프랜차이즈/브랜드명이 보이면 적고, 안 보이면 null",
  "confidence": "high | medium | low 중 하나 (인식 확신도)",
  "estimated_serving_g": 예상 1인분 중량(그램, 숫자만)
}

사진에서 음식이 명확히 보이지 않으면 food_name을 "인식불가"로 설정해.
"""

TEXT_FOOD_EXTRACTION_PROMPT = """사용자 메시지에서 음식명을 추출해서 아래 JSON 형식으로만 답해.
설명 문장이나 마크다운 코드블록(```json) 없이, 순수 JSON 텍스트만 출력해.

{
  "food_name": "음식 이름 (한글, 검색하기 좋게 간결하게. 예: '김치찌개', '제육볶음')",
  "brand": "프랜차이즈/브랜드명이 언급됐으면 적고, 없으면 null"
}

음식이 언급되지 않았으면 food_name을 "인식불가"로 설정해.
"""

MATCH_SCORE_THRESHOLD = 70  # DB 유사도 매칭 최소 점수


def parse_gemini_json(text: str) -> dict:
    """Gemini가 ```json 코드블록으로 감싸서 응답하는 경우까지 안전하게 파싱"""
    import json
    import re

    cleaned = text.strip()
    cleaned = re.sub(r"^```json\s*|\s*```$", "", cleaned, flags=re.MULTILINE).strip()
    return json.loads(cleaned)


def _build_recognize_response(
    matched: bool,
    food_name: str,
    db_match: dict | None,
    baseline: float,
    diagnosis_group: str,
) -> dict:
    """음식 인식 결과를 명세서 형식의 응답 dict로 구성"""
    if matched and db_match:
        # 예상 혈당 상승량 계산
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
    else:
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


@app.post("/rag/intake-logs/recognize")
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

    # 진단군 기본값
    diag = diagnosis_group if diagnosis_group in DIAGNOSIS_GROUPS else "건강군"

    # 식전 혈당 기본값
    bl = baseline if baseline is not None else get_pre_glucose_default(diag)

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
            return JSONResponse(
                content={"error": "인식 결과 파싱 실패", "raw": response.text},
                status_code=500,
                media_type="application/json; charset=utf-8",
            )

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
        return JSONResponse(
            content=_build_recognize_response(False, food_name, None, bl, diag),
            media_type="application/json; charset=utf-8",
        )

    # DB 매칭
    db_match = food_db.get_best_match(food_name, brand=brand)

    # ── DEBUG ── 매칭 과정 추적 (버그 해결 후 삭제)
    print(f"[DEBUG recognize] food_name={food_name!r}, brand={brand!r}")
    print(f"[DEBUG recognize] db_match={db_match}")
    # ── /DEBUG ──

    matched = db_match is not None and db_match["match_score"] >= MATCH_SCORE_THRESHOLD

    return JSONResponse(
        content=_build_recognize_response(matched, food_name, db_match if matched else None, bl, diag),
        media_type="application/json; charset=utf-8",
    )


# ---------------------------------------------------------
# 6-2. 음식 AI 재분석 엔드포인트 (사진 or 텍스트)
#      Spring이 /api/intake-logs/reanalyze → 여기로 내부 호출
#      DD_101: 사용자가 "틀려요, AI로 분석하기" 선택 시에만 호출
# ---------------------------------------------------------
FOOD_REANALYSIS_IMAGE_PROMPT = """이 사진 속 음식을 분석해서 아래 JSON 형식으로만 답해.
설명 문장이나 마크다운 코드블록(```json) 없이, 순수 JSON 텍스트만 출력해.

{
  "food_name": "음식 이름 (한글, 간결하게)",
  "serving_size": 예상 1인분 중량(그램, 숫자만),
  "nutrition": {
    "carb": 100g당_탄수화물_그램_숫자,
    "sugar": 100g당_당류_그램_숫자,
    "protein": 100g당_단백질_그램_숫자,
    "fat": 100g당_지방_그램_숫자,
    "fiber": 100g당_식이섬유_그램_숫자,
    "calorie": 100g당_칼로리_숫자
  }
}

사진을 정밀하게 분석해서 음식 종류를 판별하고, 일반적인 조리법과 재료를 기준으로
영양성분을 최대한 합리적으로 추정해.
"""

FOOD_REANALYSIS_TEXT_PROMPT = """다음 음식의 영양성분을 추정해서 아래 JSON 형식으로만 답해.
설명 문장이나 마크다운 코드블록(```json) 없이, 순수 JSON 텍스트만 출력해.

{
  "food_name": "음식 이름 (한글, 간결하게)",
  "serving_size": 예상 1인분 중량(그램, 숫자만),
  "nutrition": {
    "carb": 100g당_탄수화물_그램_숫자,
    "sugar": 100g당_당류_그램_숫자,
    "protein": 100g당_단백질_그램_숫자,
    "fat": 100g당_지방_그램_숫자,
    "fiber": 100g당_식이섬유_그램_숫자,
    "calorie": 100g당_칼로리_숫자
  }
}

해당 음식의 일반적인 조리법과 재료를 기준으로 영양성분을 최대한 합리적으로 추정해.
"""


@app.post("/rag/intake-logs/reanalyze")
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

    ※ CUSTOM_FOOD 테이블 저장은 Spring 쪽에서 처리.
       FastAPI는 추정 결과만 반환한다.
    """
    if not image and not food_name:
        return JSONResponse(
            content={"error": "image 또는 food_name 중 하나는 필수입니다."},
            status_code=400,
            media_type="application/json; charset=utf-8",
        )

    diag = diagnosis_group if diagnosis_group in DIAGNOSIS_GROUPS else "건강군"
    bl = baseline if baseline is not None else get_pre_glucose_default(diag)

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
        return JSONResponse(
            content={"error": "AI 분석 결과 파싱 실패", "raw": response.text},
            status_code=500,
            media_type="application/json; charset=utf-8",
        )

    nutrition = analysis.get("nutrition", {})
    required_keys = ["carb", "protein", "fat", "fiber"]

    if not all(k in nutrition for k in required_keys):
        return JSONResponse(
            content={"error": "AI가 영양성분을 추정하지 못했습니다.", "raw": analysis},
            status_code=500,
            media_type="application/json; charset=utf-8",
        )

    # 예상 혈당 상승량 계산
    prediction = glucose_predictor.predict_peak(
        carb=float(nutrition["carb"]),
        protein=float(nutrition["protein"]),
        fat=float(nutrition["fat"]),
        fiber=float(nutrition["fiber"]),
        baseline=bl,
        diagnosis_group=diag,
    )

    return JSONResponse(
        content={
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
        },
        media_type="application/json; charset=utf-8",
    )


# ---------------------------------------------------------
# 7. 걷기 미션 계산 유틸 (/rag/intake-logs/predict 등에서 공용으로 사용)
# ---------------------------------------------------------
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
    distance_km = round(minutes * 0.06, 2)  # 분당 약 60m 도보 가정
    calories = round(minutes * 4)  # 분당 약 4kcal 소모 가정

    return {"walk_minutes": minutes, "distance_km": distance_km, "calories": calories}


# ---------------------------------------------------------
# 6-3. 음식 최종 확정 시 portion 반영 재예측 엔드포인트
#      Spring이 POST /api/intake-logs(음식 최종 확정) 처리 중
#      -> 여기로 내부 호출
#
#      Spring은 food_no/custom_food_no로 이미 알고 있는 영양성분
#      (1 serving_size 기준)에 사용자가 선택한 portion(0.5/1.0/1.5 등)을
#      곱해서 보내면, FastAPI가 LightGBM으로 재예측하고 걷기 미션
#      목표(targetDistance/targetKcal)까지 계산해서 돌려준다.
# ---------------------------------------------------------
class PortionPredictRequest(BaseModel):
    carb: float
    sugar: float = 0
    protein: float
    fat: float
    fiber: float
    calorie: float = 0
    portion: float = 1.0  # 섭취 비율 (예: 0.5 / 1.0 / 1.5)
    baseline: float  # 식전 혈당 (preGlucose)
    diagnosis_group: str  # "건강군" | "전당뇨" | "2형당뇨"


@app.post("/rag/intake-logs/predict")
def predict_with_portion(req: PortionPredictRequest):
    """
    portion이 반영된 최종 예상 혈당 상승량 + 추천 걷기 미션을 계산한다.

    nutrition(carb/sugar/protein/fat/fiber/calorie)은 "1 serving_size 기준"
    값으로 받는다 (100g 기준이 아님 — Spring이 FOOD_INFO/CUSTOM_FOOD에서
    가져온 값을 그대로 전달). portion을 곱해서 실제 섭취량을 반영한다.
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
            "lowConfidence": prediction["low_confidence"],
            "targetDistance": mission["distance_km"],
            "targetKcal": mission["calories"],
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


