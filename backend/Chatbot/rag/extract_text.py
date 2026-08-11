# -*- coding: utf-8 -*-
"""
sources.py의 논문 PDF들을 전부 텍스트로 추출해서,
논문별 구분 헤더를 넣어 하나의 텍스트 파일(papers_combined.txt)로 합친다.

왜 이렇게 하나?
- Gemini는 PDF를 페이지 이미지처럼 처리해서(비전 모델 경유) 느리고 토큰도 많이 씀
- 순수 텍스트는 이 과정이 없어 훨씬 빠르고 저렴함
- 파일 여러 개보다 하나로 합쳐두면 업로드/캐시 관리도 단순해짐
- 다만 출처 구분이 안 되면 인용이 부정확해지니, 논문별 헤더를 넣어서 합침

사용법:
    python extract_text.py
    (한 번만 실행하면 됨. papers_combined.txt가 이미 있으면 자동으로 재사용됨 -
     새 논문 추가했으면 다시 실행)
"""

import re
from pathlib import Path

import pdfplumber

try:
    from .sources import SOURCES
except ImportError:
    # extract_text.py를 rag 폴더 안에서 직접 실행할 때(python extract_text.py)를 위한 대비
    from sources import SOURCES

SCRIPT_DIR = Path(__file__).parent
OUTPUT_PATH = SCRIPT_DIR / "papers_combined.txt"

# 이 줄(정규화 후 일치, 짧은 제목 줄 기준)이 나오면 그 지점부터 논문 끝까지 통째로 버림.
# 참고문헌 목록, 부록(원자료 표 등)은 분량은 큰데 챗봇 답변에는 거의 안 쓰이기 때문.
STOP_SECTION_MARKERS = [
    "참고문헌", "references", "reference", "literaturecited", "appendix", "부록",
]


def _normalize(line: str) -> str:
    """공백/□·-·* 같은 장식 특수문자를 제거하고 소문자로 통일"""
    cleaned = re.sub(r"[^0-9a-zA-Z가-힣]", "", line)
    return cleaned.lower()


def is_stop_marker_line(line: str) -> bool:
    stripped = line.strip()
    if not stripped or len(stripped) > 30:
        return False  # 너무 긴 줄은 본문 문장일 가능성이 높아 오탐 방지차 제외
    normalized = _normalize(stripped)
    if not normalized:
        return False
    return any(normalized == marker for marker in STOP_SECTION_MARKERS)


# 참고문헌 구간을 건너뛰다가 이 패턴이 나오면 "새 소제목/새 절 시작"으로 보고 다시 포함시킴.
# 예: "1-1. 당뇨병 진단 및 분류"(장-절 번호), "6장. 약물치료"(장 번호) 처럼 구조적인 번호 매김.
# 주의: 단순 "1. ", "2. " 처럼 숫자 하나짜리는 참고문헌 목록 번호("1. Author...")와 구분이
# 안 되므로 재개 조건에서 일부러 제외함 (넣으면 참고문헌이 거의 안 잘림).
_RESUME_MARKER_RE = re.compile(r"^\d+-\d+\s*[.\)]\s*\S|^\d+\s*장[.\s]")


def is_resume_marker_line(line: str) -> bool:
    stripped = line.strip()
    if not stripped or len(stripped) > 60:
        return False
    if detect_section_header(line):
        return True
    return bool(_RESUME_MARKER_RE.match(stripped))


def smart_trim(text: str) -> str:
    """
    참고문헌/부록 구간을 건너뛰되, 그 뒤에 새 소제목(장/절 번호 등)이 나오면
    다시 포함시킴. 논문처럼 참고문헌이 "맨 끝에 한 번만" 있는 문서는 결과적으로
    끝까지 잘리고, 진료지침처럼 "절마다 참고문헌이 반복되는" 문서는 그 구간만
    건너뛰고 다음 절부터 이어서 살아남음.
    """
    kept_lines = []
    skipping = False

    for line in text.splitlines():
        if not skipping and is_stop_marker_line(line):
            skipping = True
            continue
        if skipping:
            if is_resume_marker_line(line):
                skipping = False
                kept_lines.append(line)
            continue
        kept_lines.append(line)

    return "\n".join(kept_lines)


# 논문/보고서에서 흔히 쓰이는 소제목 이름들 (정규화 후 비교).
# 이 목록에 있는 이름과 일치하는 짧은 줄이 나오면 "소제목"으로 인식해서 그 지점부터 새 섹션 시작.
SECTION_NAME_SET = {
    "서론", "연구목적", "목적", "연구방법", "연구대상및방법", "대상및방법",
    "재료및방법", "연구방법및절차", "실험방법", "재료및전처리",
    "연구결과", "결과", "실험결과및분석", "고찰", "논의", "결론",
    "요약", "요약및결론", "초록", "abstract", "제언", "주요결과요약",
    "지표정의", "추진배경", "추진배경및목적",
}

# 줄 맨 앞에 붙는 번호 매김 형태(로마숫자, "제N장", "N." 등)를 떼어내기 위한 정규식.
# OCR로 스캔한 문서는 로마숫자(Ⅰ,Ⅱ,Ⅴ 등)가 "[", "\\" 같은 엉뚱한 특수문자로 잘못
# 인식되는 경우가 많아서, 그런 leftover 특수문자 접두어도 같이 떼어내도록 넉넉하게 잡음.
_NUMBERING_PREFIX = re.compile(
    r"^(?:[ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩ]+\s*[.\)]?\s*|제\s*\d+\s*장\s*|\d+\s*[.\)]\s*|[^\w가-힣\s]{1,3}\s*[.\)]?\s*)?(.+)$"
)


def detect_section_header(line: str) -> str | None:
    """
    이 줄이 소제목(서론/연구방법/결과/고찰/결론 등)인지 판별.
    맞으면 정규화된 소제목 이름을 반환, 아니면 None.
    """
    stripped = line.strip()
    if not stripped or len(stripped) > 40:
        return None

    match = _NUMBERING_PREFIX.match(stripped)
    if not match:
        return None

    candidate = re.sub(r"\s+", "", match.group(1)).lower()
    if candidate in SECTION_NAME_SET:
        return match.group(1).strip()  # 화면 표시용으로는 원래 표기(공백 포함) 유지
    return None


def split_into_sections(text: str) -> list[tuple[str, str]]:
    """
    텍스트를 소제목 기준으로 나눔.
    반환: [(소제목, 본문내용), ...]. 첫 소제목 이전 내용은 "(머리말)"로 묶임.
    같은 소제목이 페이지 반복 헤더 등으로 연달아 다시 나오면 새 섹션을 만들지 않고 이어붙임.
    """
    result = []
    current_title = "(머리말)"
    current_lines: list[str] = []

    def _normalized_title(t: str) -> str:
        return re.sub(r"\s+", "", t).lower()

    for line in text.splitlines():
        header = detect_section_header(line)
        if header and _normalized_title(header) != _normalized_title(current_title):
            if current_lines:
                result.append((current_title, "\n".join(current_lines).strip()))
            current_title = header
            current_lines = []
        elif header:
            # 같은 소제목이 반복 등장(페이지 헤더 등) -> 새 섹션 만들지 않고 무시
            continue
        else:
            current_lines.append(line)

    if current_lines:
        result.append((current_title, "\n".join(current_lines).strip()))

    return result


def extract_pdf_text_two_column(pdf_path: Path, trim_low_value_sections: bool = True) -> str:
    """
    2단 컬럼 레이아웃 PDF용 추출 함수.
    페이지를 좌/우 절반으로 잘라서 왼쪽 칸을 먼저, 오른쪽 칸을 나중에 추출함으로써
    좌우 텍스트가 한 줄에 섞이는 문제를 방지함.
    """
    text_parts = []
    stopped = False

    with pdfplumber.open(pdf_path) as pdf:
        for page in pdf.pages:
            if stopped:
                break

            mid_x = page.width / 2
            left = page.crop((0, 0, mid_x, page.height))
            right = page.crop((mid_x, 0, page.width, page.height))

            page_text = "\n".join(
                t for t in [left.extract_text(), right.extract_text()] if t
            )
            if not page_text:
                continue

            if not trim_low_value_sections:
                text_parts.append(page_text)
                continue

            kept_lines = []
            for line in page_text.splitlines():
                if is_stop_marker_line(line):
                    stopped = True
                    break
                kept_lines.append(line)

            if kept_lines:
                text_parts.append("\n".join(kept_lines))

    return "\n".join(text_parts)


def extract_pdf_text(pdf_path: Path, trim_low_value_sections: bool = True) -> str:
    text_parts = []
    stopped = False

    with pdfplumber.open(pdf_path) as pdf:
        for page in pdf.pages:
            if stopped:
                break

            page_text = page.extract_text()
            if not page_text:
                continue

            if not trim_low_value_sections:
                text_parts.append(page_text)
                continue

            kept_lines = []
            for line in page_text.splitlines():
                if is_stop_marker_line(line):
                    stopped = True
                    break
                kept_lines.append(line)

            if kept_lines:
                text_parts.append("\n".join(kept_lines))

    return "\n".join(text_parts)


def _find_nth_occurrence(text: str, marker: str, n: int) -> int:
    """marker가 n번째로 나오는 위치를 반환. 없으면 -1"""
    idx = -1
    for _ in range(n):
        idx = text.find(marker, idx + 1)
        if idx == -1:
            return -1
    return idx


def _apply_slice_range(source: dict, text: str) -> str:
    """
    source에 slice_start_marker/slice_end_marker가 지정되어 있으면
    그 구간만 잘라서 반환 (예: 185페이지짜리 문서에서 특정 장만 쓰고 싶을 때).
    """
    start_marker = source.get("slice_start_marker")
    end_marker = source.get("slice_end_marker")

    start_idx = 0
    if start_marker:
        occurrence = source.get("slice_start_occurrence", 1)
        found = _find_nth_occurrence(text, start_marker, occurrence)
        if found != -1:
            start_idx = found

    end_idx = len(text)
    if end_marker:
        occurrence = source.get("slice_end_occurrence", 1)
        found = _find_nth_occurrence(text, end_marker, occurrence)
        if found != -1:
            end_idx = found

    return text[start_idx:end_idx]


def get_source_text(source: dict) -> tuple[str, str]:
    """
    소스 1개의 (trim된 텍스트, 원본 텍스트)를 반환.
    - ocr_override 지정된 경우: 미리 뽑아둔 OCR 텍스트 파일 사용 (스캔본 PDF 대응)
    - two_column 지정된 경우: 좌/우 컬럼 분리 추출
    - slice_start_marker/slice_end_marker 지정된 경우: 그 구간만 사용 (긴 문서 일부만 쓸 때)
    - 그 외: 기본 추출
    """
    ocr_path = source.get("ocr_override")
    if ocr_path:
        full_ocr_path = SCRIPT_DIR / ocr_path
        if full_ocr_path.exists():
            raw = full_ocr_path.read_text(encoding="utf-8")
            raw = _apply_slice_range(source, raw)
            return smart_trim(raw), raw
        print(f"  ⚠️ OCR 캐시 파일 없음: {full_ocr_path}")

    full_path = SCRIPT_DIR / source["file_path"]
    extractor = extract_pdf_text_two_column if source.get("two_column") else extract_pdf_text

    raw_full = extractor(full_path, trim_low_value_sections=False)
    raw_sliced = _apply_slice_range(source, raw_full)

    return smart_trim(raw_sliced), raw_sliced


def format_paper_with_toc(source: dict, text: str) -> tuple[str, int]:
    """논문 1개를 [목차] + 소제목별 본문 형태로 포맷팅. (포맷된 텍스트, 인식된 소제목 개수) 반환"""
    sections = split_into_sections(text)

    toc_titles = [title for title, _ in sections if title != "(머리말)"]
    toc_block = "\n".join(f"- {t}" for t in toc_titles) if toc_titles else "(소제목 인식 안 됨)"

    header = (
        f"\n\n{'=' * 20}\n"
        f"[논문 제목] {source['title']}\n"
        f"[저자] {source.get('authors', '정보없음')}\n"
        f"[발행연도] {source.get('published_year', '정보없음')}\n"
        f"{'=' * 20}\n\n"
        f"[목차]\n{toc_block}\n"
    )

    body_parts = []
    for title, content in sections:
        if not content:
            continue
        body_parts.append(f"\n[{title}]\n{content}")

    return header + "".join(body_parts), len(toc_titles)


def build_combined_text() -> str:
    sections = []
    total_raw_chars = 0
    total_trimmed_chars = 0

    for source in SOURCES:
        full_path = SCRIPT_DIR / source["file_path"]
        ocr_path = source.get("ocr_override")
        ocr_full_path = SCRIPT_DIR / ocr_path if ocr_path else None

        if not full_path.exists() and not (ocr_full_path and ocr_full_path.exists()):
            print(f"파일 없음, 건너뜀: {full_path}")
            continue

        print(f"추출 중: {source['title']}")
        text, raw_text = get_source_text(source)

        total_raw_chars += len(raw_text)
        total_trimmed_chars += len(text)
        cut = len(raw_text) - len(text)
        if cut > 0:
            print(f"  참고문헌/부록 등 {cut:,}자 절삭됨 ({len(raw_text):,}자 -> {len(text):,}자)")

        if len(text) == 0 and not ocr_path:
            print("  ⚠️ 텍스트 추출 0자 — 스캔 이미지 PDF일 가능성 높음 (OCR 필요, sources.py에 ocr_override 추가 필요)")

        formatted, section_count = format_paper_with_toc(source, text)
        print(f"  소제목 {section_count}개로 분할됨")

        sections.append(formatted)

    if total_raw_chars > 0:
        pct = (total_raw_chars - total_trimmed_chars) / total_raw_chars * 100
        print(f"\n전체 절삭률: {pct:.1f}% ({total_raw_chars:,}자 -> {total_trimmed_chars:,}자)")

    return "".join(sections)


def main():
    combined = build_combined_text()
    OUTPUT_PATH.write_text(combined, encoding="utf-8")
    print(f"\n완료: {OUTPUT_PATH} ({len(combined):,}자)")


if __name__ == "__main__":
    main()
