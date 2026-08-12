# -*- coding: utf-8 -*-
"""
당당이가 답변할 때 참고할 논문/보고서 목록 (로컬 PDF).

RAG(벡터DB, 임베딩 검색) 없이, Gemini에 PDF를 직접 첨부해서
"이 논문들 내용을 참고해서 답해줘" 방식으로 사용한다.

file_path는 이 파일(sources.py) 기준 상대경로.
"""

SOURCES = [
    {
        "title": "걷기량과 신체활동이 제2형 당뇨병환자들의 혈당에 미치는 영향",
        "authors": "제갈윤석 외 9인",
        "published_year": 2008,
        "file_path": "papers/걷기량과 신체활동이 제2형 당뇨환자들의 혈당 에미치는 영향.pdf",
        "two_column": True,
    },
    {
        "title": "제2형 당뇨병 환자들의 운동량이 신체조성 분석 및 혈당, HbA1c 감소에 미치는 영향",
        "authors": "윤상호",
        "published_year": 2007,
        "file_path": "papers/제2형 당뇨병 환자들의 운동량이 신체조성 분석 및 혈당, HbA1c 감소에 미치는 영향.p.pdf",
        # 이 PDF는 텍스트 레이어가 없는 스캔 이미지라 pdfplumber로 추출이 안 됨.
        # 미리 OCR로 뽑아둔 텍스트 파일을 대신 사용함.
        "ocr_override": "ocr_cache/제2형_당뇨병_환자들의_운동량이_신체조성_분석_및_혈당__HbA1c_감소에_미치는_영향_p.txt",
    },
    {
        "title": "한국인 상용 식품의 혈당지수(Glycemic Index) 추정치를 활용한 한국 성인의 식사혈당지수 산출",
        "authors": "송수진 외",
        "published_year": 2012,
        "file_path": "papers/한국인 상용 식품의 혈당지수 (Glycemic Index) 추정치를 활용한 한국 성인의 식.pdf",
        "two_column": True,
    },
    {
        "title": "성인에서 착즙 주스와 탄수화물 식품 섭취 시 혈당 반응에 미치는 영향: Pilot Study",
        "authors": "최윤지 외 (인제대학교)",
        "published_year": 2026,
        "file_path": "papers/성인에서 착즙 주스와 탄수화물 식품 섭취 시 혈당 반응에 미치는 영향 Pilot Study.pdf",
        "two_column": True,  # 2단 컬럼 레이아웃이라 좌/우 분리 추출 필요
    },
    {
        "title": "국민건강영양조사 기반의 당뇨병 관리지표 심층보고서",
        "authors": "질병관리청",
        "published_year": 2023,
        "file_path": "papers/2023년_심층보고서_2호_국민건강영양조사+기반의+당뇨병+관리지표+심층보고서.pdf",
    },
    {
        "title": "고혈압, 당뇨병, 고콜레스테롤혈증 유병 및 관련요인 추이",
        "authors": "김윤정, 김혜진, 오경원 (질병관리청)",
        "published_year": 2023,
        "file_path": "papers/2023년_2호_현안보고서_고혈압,+당뇨병,+고콜레스테롤혈증+유병+및+관련요인+추이 (1).pdf",
    },
    {
        "title": "2025 당뇨병 진료지침 (1~5장: 진단/예방/혈당조절목표/모니터링/자기관리)",
        "authors": "대한당뇨병학회 진료지침위원회",
        "published_year": 2025,
        "file_path": "papers/2025 당뇨병 진료지침_전문_최종본.pdf",
        "two_column": True,
        # 185페이지 전체 중 1~5장(당뇨병 분류/진단, 2형당뇨병 예방, 혈당조절 목표,
        # 혈당 모니터링, 포괄적 자기관리·운동요법·의학영양요법)만 사용.
        # 6장(약물치료)부터는 당당이 서비스 범위(식후 혈당 관리, 생활습관)와 거리가 있어 제외.
        "slice_start_marker": "1-1. 당뇨병 진단 및 분류",
        "slice_start_occurrence": 2,  # 1번째는 목차에 나온 것, 2번째가 실제 본문 시작
        "slice_end_marker": "6-1. 1형당뇨병의 약물치료",
        "slice_end_occurrence": 3,  # 1,2번째는 요약/목차, 3번째가 실제 6장 시작 (여기서 잘라냄)
        # ⚠️ 저작권 주의: 대한당뇨병학회 원문(무단 배포 금지 명시).
        # 비영리 프로젝트 내부 RAG 용도로만 사용, 외부 재배포 금지.
    },
]