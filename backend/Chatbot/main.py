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


def get_diagnosis_group(user_id: str) -> str:
    return user_diagnosis_groups.get(user_id, "건강군")


def hba1c_to_diagnosis_group(hba1c: float) -> str:
    """
    대한당뇨병학회/ADA 기준 HbA1c → 진단군 변환

    < 5.7%       : 건강군
    5.7% ~ 6.4%  : 전당뇨
    >= 6.5%      : 2형당뇨

    주의: 이건 선별(screening) 목적의 참고 기준이지 의학적 확진이 아님.
    실제 진단군은 사용자가 병원에서 진단받은 결과를 우선하는 게 맞고,
    HbA1c는 진단명을 모르는 회원가입 시점의 "추정용 폴백"으로 쓰는 걸 추천.
    """
    if hba1c < 5.7:
        return "건강군"
    elif hba1c < 6.5:
        return "전당뇨"
    else:
        return "2형당뇨"


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


class SignupRequest(BaseModel):
    user_id: str
    hba1c: float


@app.post("/signup-profile")
def signup_profile(req: SignupRequest):
    """회원가입 시 HbA1c만 받아서 진단군을 자동 계산 후 저장"""
    diagnosis_group = hba1c_to_diagnosis_group(req.hba1c)
    user_diagnosis_groups[req.user_id] = diagnosis_group

    return JSONResponse(
        content={
            "user_id": req.user_id,
            "hba1c": req.hba1c,
            "diagnosis_group": diagnosis_group,
        },
        media_type="application/json; charset=utf-8",
    )


# ---------------------------------------------------------
# 자연어 메시지에서 "음식 + 혈당 수치" 언급 추출
# ---------------------------------------------------------
MEAL_EXTRACTION_PROMPT = """사용자 메시지에서 "방금/오늘 먹은 음식"과 "현재 또는 식전 혈당 수치(mg/dL)"가
함께 언급되었는지 확인해서 아래 JSON 형식으로만 답해. 설명 문장 없이 순수 JSON만 출력해.

{
  "has_meal_info": true 또는 false,
  "food_name": "언급된 음식명 (간결하게, 없으면 null)",
  "baseline": 혈당_숫자_또는_null
}

혈당 수치와 음식 둘 다 언급된 경우에만 has_meal_info를 true로 해.
"""


def extract_meal_info(message: str) -> dict:
    response = client.models.generate_content(
        model=MODEL_NAME,
        contents=message,
        config={"system_instruction": MEAL_EXTRACTION_PROMPT, "temperature": 0.0},
    )
    log_token_usage(response, label="meal-extraction")
    try:
        return parse_gemini_json(response.text)
    except Exception:
        return {"has_meal_info": False, "food_name": None, "baseline": None}


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


@app.post("/chat")
def chat(req: ChatRequest):
    # 인슐린/약물 용량 관련 질문은 Gemini 호출 전에 키워드로 먼저 차단
    if is_medication_dosage_question(req.message):
        return JSONResponse(
            content={"reply": MEDICATION_SAFETY_MESSAGE, "prediction_data": None},
            media_type="application/json; charset=utf-8",
        )

    if req.diagnosis_group:
        user_diagnosis_groups[req.user_id] = req.diagnosis_group

    user_context = get_dummy_user_context(req.user_id)
    system_prompt = SYSTEM_PROMPT_TEMPLATE.format(
        knowledge=FIXED_KNOWLEDGE,
        user_context=user_context,
    )

    chat_session = get_or_create_chat_session(req.user_id, system_prompt)

    # 1) 메시지에 "음식 + 혈당 수치"가 함께 언급됐는지 먼저 확인
    meal_info = extract_meal_info(req.message)

    message_to_send = req.message
    prediction_data = None  # 구조화된 예측 결과 (프론트엔드 카드 UI용, 있으면 채워짐)

    if meal_info.get("has_meal_info") and meal_info.get("food_name") and meal_info.get("baseline") is not None:
        prediction_result = run_glucose_prediction(
            food_name=meal_info["food_name"],
            baseline=float(meal_info["baseline"]),
            diagnosis_group=get_diagnosis_group(req.user_id),
        )

        if "error" not in prediction_result:
            prediction_data = prediction_result

            estimate_note = (
                " (이 음식은 정확한 DB 정보가 없어서 대략적으로 추정한 영양성분 기준이라는 것도 언급해줘.)"
                if prediction_result["nutrition_source"] == "llm_estimated"
                else ""
            )

            # 예측 결과를 당당이가 자연스러운 말투로, 하지만 핵심 수치(예상 혈당, 걷기 시간·거리)는
            # 반드시 문장 안에 포함해서 답하도록 컨텍스트에 실어 보냄
            message_to_send = (
                f"{req.message}\n\n"
                f"[내부 참고용 예측 결과 — 아래 수치를 반드시 답변 문장에 자연스럽게 포함해서 말해줘. "
                f"예: '지금 {{baseline}}에서 약 {{상승분}} 올라서 {{peak}} 근처일 것 같아요. "
                f"{{걷기시간}}분, 약 {{거리}}km 정도 걸어보세요'처럼 "
                f"식전혈당/예상peak혈당/추천 걷기시간(분)/걷기거리(km)는 꼭 숫자로 알려줘.{estimate_note} "
                f"이 수치는 학습된 예측 모델이 직접 계산한 값이니, 이전 대화에서 첨부됐던 논문이나 "
                f"다른 참고자료를 이번 답변에는 인용하지 마 (이번 답변은 논문 근거가 아님)]\n"
                f"매칭된 음식: {prediction_result['matched_food']}\n"
                f"영양성분 출처: {'DB 매칭' if prediction_result['nutrition_source'] == 'db_matched' else 'LLM 추정치'}\n"
                f"식전 혈당: {prediction_result['prediction']['baseline']}\n"
                f"예측 peak 혈당: {prediction_result['prediction']['predicted_peak']}\n"
                f"상승분: {prediction_result['prediction']['predicted_rise']}\n"
                f"추천 걷기 시간: {prediction_result['walking_mission']['walk_minutes']}분\n"
                f"추천 걷기 거리: {prediction_result['walking_mission']['distance_km']}km\n"
            )
        else:
            # DB 매칭 실패 등은 그냥 알려주고 일반 대화로 넘어감
            message_to_send = (
                f"{req.message}\n\n"
                f"[참고: 예측 시도했으나 실패함 - {prediction_result['error']}. "
                f"이 사실은 사용자에게 자연스럽게 알려주되 너무 기술적으로 말하지 마]"
            )
    else:
        # 2) 음식/혈당 언급이 없는 일반 질문 -> 논문 텍스트 컨텍스트로 답변
        if paper_cache:
            # 캐시가 있으면 논문을 매번 다시 안 보내고 캐시만 참조 (훨씬 빠름)
            response = answer_with_paper_cache(client, MODEL_NAME, req.message, paper_cache)
            log_token_usage(response, label="chat-paper-cached")

            return JSONResponse(
                content={"reply": response.text, "prediction_data": None},
                media_type="application/json; charset=utf-8",
            )
        elif combined_papers_text:
            # 캐시 생성이 실패했을 때의 폴백 (느리지만 동작은 함)
            response = answer_without_cache(client, MODEL_NAME, req.message, combined_papers_text)
            log_token_usage(response, label="chat-paper-nocache")

            return JSONResponse(
                content={"reply": response.text, "prediction_data": None},
                media_type="application/json; charset=utf-8",
            )

    response = chat_session.send_message(message_to_send)

    log_token_usage(response, label="chat")

    # PowerShell(Windows) 등 일부 클라이언트가 charset 없는 응답을
    # 잘못 해석해 한글이 깨지는 문제 방지 위해 charset 명시
    return JSONResponse(
        content={"reply": response.text, "prediction_data": prediction_data},
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
    matched = db_match is not None and db_match["match_score"] >= MATCH_SCORE_THRESHOLD

    return JSONResponse(
        content=_build_recognize_response(matched, food_name, db_match if matched else None, bl, diag),
        media_type="application/json; charset=utf-8",
    )


# ---------------------------------------------------------
# 7. 혈당 예측 + 걷기 미션 엔드포인트
# ---------------------------------------------------------
class PredictRequest(BaseModel):
    food_name: str
    brand: str | None = None
    serving_g: float = 100  # 실제 섭취량(g). DB는 100g 기준이라 이 값으로 스케일링
    baseline: float  # 식전 혈당 (mg/dL)
    diagnosis_group: str  # "건강군" | "전당뇨" | "2형당뇨"


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


NUTRITION_ESTIMATION_PROMPT = """다음 음식의 100g당 영양성분을 추정해서 아래 JSON 형식으로만 답해.
설명 문장 없이 순수 JSON만 출력해.

{
  "carb": 100g당_탄수화물_그램_숫자,
  "sugar": 100g당_당류_그램_숫자,
  "protein": 100g당_단백질_그램_숫자,
  "fat": 100g당_지방_그램_숫자,
  "fiber": 100g당_식이섬유_그램_숫자,
  "calorie": 100g당_칼로리_숫자
}

일반적으로 알려진 조리법과 재료를 기준으로 최대한 합리적인 값을 추정해.
"""


def estimate_nutrition_via_llm(food_name: str) -> dict:
    """DB 매칭 실패 시 텍스트 음식명만으로 Gemini에게 영양성분(100g당)을 추정시킴"""
    response = client.models.generate_content(
        model=MODEL_NAME,
        contents=food_name,
        config={"system_instruction": NUTRITION_ESTIMATION_PROMPT, "temperature": 0.3},
    )
    log_token_usage(response, label="nutrition-estimation")
    try:
        return parse_gemini_json(response.text)
    except Exception:
        return {}


def run_glucose_prediction(
    food_name: str,
    baseline: float,
    diagnosis_group: str,
    brand: str | None = None,
    serving_g: float = 100,
) -> dict:
    """/predict-glucose와 /chat(자연어 파서)이 공용으로 쓰는 예측 로직"""
    if diagnosis_group not in DIAGNOSIS_GROUPS:
        return {"error": f"diagnosis_group은 {DIAGNOSIS_GROUPS} 중 하나여야 합니다."}

    db_match = food_db.get_best_match(food_name, brand=brand)
    MATCH_SCORE_THRESHOLD = 70

    if db_match and db_match["match_score"] >= MATCH_SCORE_THRESHOLD:
        nutrition_source = "db_matched"
        matched_name = db_match["food_name"]
        per_100g = {
            "carb": db_match["carb"],
            "protein": db_match["protein"],
            "fat": db_match["fat"],
            "fiber": db_match["fiber"],
        }
    else:
        # DB에 없으면 Gemini로 영양성분 추정 (reanalyze 엔드포인트에서 호출)
        estimated = estimate_nutrition_via_llm(food_name)
        required_keys = ["carb", "protein", "fat", "fiber"]
        if not estimated or not all(k in estimated for k in required_keys):
            return {"error": f"'{food_name}'에 해당하는 음식을 DB에서도, LLM 추정으로도 찾지 못했습니다."}

        nutrition_source = "llm_estimated"
        matched_name = food_name
        per_100g = estimated

    scale = serving_g / 100.0
    carb = per_100g["carb"] * scale
    protein = per_100g["protein"] * scale
    fat = per_100g["fat"] * scale
    fiber = per_100g["fiber"] * scale

    prediction = glucose_predictor.predict_peak(
        carb=carb, protein=protein, fat=fat, fiber=fiber,
        baseline=baseline, diagnosis_group=diagnosis_group,
    )

    mission = calc_walking_mission(prediction["predicted_peak"])

    message = (
        f"지금 {prediction['baseline']}에서 약 {prediction['predicted_rise']} 정도 올라서 "
        f"{prediction['predicted_peak']} 근처일 것 같아요. "
        f"{mission['walk_minutes']}분(약 {mission['distance_km']}km) 정도 걸으면 도움이 될 거예요!"
    )
    if nutrition_source == "llm_estimated":
        message += " (다만 이 음식은 정확한 영양정보가 없어서 대략적으로 추정한 값이에요.)"
    if prediction["low_confidence"]:
        message += " (또한 지금 식전 혈당이 학습 데이터 범위보다 높아서, 이 예측은 참고용으로만 봐주세요.)"

    return {
        "matched_food": matched_name,
        "nutrition_source": nutrition_source,  # "db_matched" | "llm_estimated"
        "serving_g": serving_g,
        "nutrition_used": {
            "carb": round(carb, 1),
            "protein": round(protein, 1),
            "fat": round(fat, 1),
            "fiber": round(fiber, 1),
        },
        "prediction": prediction,
        "walking_mission": mission,
        "message": message,
    }


@app.post("/predict-glucose")
def predict_glucose(req: PredictRequest):
    result = run_glucose_prediction(
        food_name=req.food_name,
        baseline=req.baseline,
        diagnosis_group=req.diagnosis_group,
        brand=req.brand,
        serving_g=req.serving_g,
    )

    status_code = 200
    if "error" in result:
        status_code = 400 if "diagnosis_group" in result["error"] else 404

    return JSONResponse(content=result, status_code=status_code, media_type="application/json; charset=utf-8")
