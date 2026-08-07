# -*- coding: utf-8 -*-
"""
RAG 지식베이스에 넣을 소스 문서 목록 (KB_DOCUMENT 스키마에 맞춘 메타데이터)

전부 대한당뇨병학회 학술지 'Journal of Korean Diabetes'의 오픈액세스(CC BY-NC) 논문.
CC BY-NC 라이선스이므로 비영리 목적(KDT 최종 프로젝트 등)에는 사용 가능하지만,
실제 상업 서비스로 전환 시에는 반드시 라이선스를 재검토해야 함.
"""

SOURCES = [
    {
        "title": "2023 당뇨병 진료지침 개정방향 (선별검사, 운동, 의학영양요법, 연속혈당측정)",
        "doc_type": "논문",
        "publisher": "대한당뇨병학회",
        "authors": "대한당뇨병학회 진료지침위원회",
        "published_year": 2023,
        "source_url": "https://synapse.koreamed.org/upload/synapsexml/0178jkd/pdf/jkd-2023-24-3-120.pdf",
        "file_path": None,  # 로컬에 파일 저장 안 하고 매번 원격 URL에서 가져옴
    },
    {
        "title": "2023 당뇨병 진료지침: 2형당뇨병의 약물치료",
        "doc_type": "논문",
        "publisher": "대한당뇨병학회",
        "authors": "노정현 (인제대학교 일산백병원)",
        "published_year": 2023,
        "source_url": "https://synapse.koreamed.org/upload/synapsexml/0178jkd/pdf/jkd-2023-24-3-127.pdf",
        "file_path": None,
    },
    {
        "title": "2023 당뇨병 진료지침: 심혈관질환 위험인자 관리",
        "doc_type": "논문",
        "publisher": "대한당뇨병학회",
        "authors": "대한당뇨병학회 진료지침위원회",
        "published_year": 2023,
        "source_url": "https://synapse.koreamed.org/upload/synapsexml/0178jkd/pdf/jkd-2023-24-3-135.pdf",
        "file_path": None,
    },
]
