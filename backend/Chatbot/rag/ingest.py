# -*- coding: utf-8 -*-
"""
RAG 지식베이스 구축 스크립트 (KB_DOCUMENT / KB_CHUNK 스키마 기준)

sources.py에 정의된 문서들을 가져와서(PDF 다운로드 -> 페이지별 텍스트 추출)
1) KB_DOCUMENT에 문서 메타데이터 1건 저장
2) 페이지 단위로 청크를 만들어 KB_CHUNK에 임베딩과 함께 저장

사용법:
    python ingest.py

환경변수 (.env):
    GEMINI_API_KEY=...
    DATABASE_URL=postgresql://user:password@localhost:5432/dbname
"""

import os
import re
import time
from io import BytesIO

import requests
import pdfplumber
import psycopg2
from pgvector.psycopg2 import register_vector
from dotenv import load_dotenv
from google import genai
from google.genai import types

from sources import SOURCES

load_dotenv()

client = genai.Client(api_key=os.environ.get("GEMINI_API_KEY"))
EMBEDDING_MODEL = "gemini-embedding-001"
EMBEDDING_DIM = 1536  # KB_CHUNK.embedding VECTOR(1536)과 반드시 일치해야 함

CHUNK_SIZE = 500       # 청크당 대략 글자 수 (chunk_text VARCHAR(1000) 한도 안에서 여유있게)
CHUNK_OVERLAP = 80     # 청크 간 겹치는 글자 수 (문맥 끊김 방지)

# 페이지 텍스트에서 이 패턴과 정확히 일치하는 줄이 나오면 "현재 섹션"으로 갱신
SECTION_HEADERS = ["서론", "본론", "결론", "참고문헌", "서 론", "본 론", "결 론"]


def fetch_pdf_pages(url: str) -> list[tuple[int, str]]:
    """URL의 PDF를 다운로드해서 (페이지번호, 페이지텍스트) 리스트로 반환"""
    resp = requests.get(url, timeout=30, headers={"User-Agent": "Mozilla/5.0"})
    resp.raise_for_status()

    pages = []
    with pdfplumber.open(BytesIO(resp.content)) as pdf:
        for i, page in enumerate(pdf.pages, start=1):
            text = page.extract_text()
            if text:
                pages.append((i, text))
    return pages


def detect_section(page_text: str, current_section: str | None) -> str | None:
    for line in page_text.splitlines():
        stripped = line.strip()
        if stripped in SECTION_HEADERS:
            return stripped.replace(" ", "")
    return current_section


def chunk_text(text: str, chunk_size: int = CHUNK_SIZE, overlap: int = CHUNK_OVERLAP) -> list[str]:
    """긴 텍스트를 겹치는 구간을 두고 청크로 분할 (chunk_text VARCHAR(1000) 한도 준수)"""
    text = re.sub(r"\s+", " ", text).strip()
    if not text:
        return []

    chunks = []
    start = 0
    while start < len(text):
        end = start + chunk_size
        chunk = text[start:end]
        chunks.append(chunk.strip())
        start = end - overlap

    return [c for c in chunks if len(c) > 30]


def embed_texts(texts: list[str]) -> list[dict]:
    """
    여러 텍스트를 임베딩. 반환: [{"values": [...], "token_count": int}, ...]
    Gemini API 배치 제한 고려해서 20개씩 나눠 호출
    """
    all_results = []
    batch_size = 20

    for i in range(0, len(texts), batch_size):
        batch = texts[i:i + batch_size]
        response = client.models.embed_content(
            model=EMBEDDING_MODEL,
            contents=batch,
            config=types.EmbedContentConfig(
                task_type="RETRIEVAL_DOCUMENT",
                output_dimensionality=EMBEDDING_DIM,
            ),
        )
        for e in response.embeddings:
            token_count = None
            if getattr(e, "statistics", None) is not None:
                token_count = e.statistics.token_count
            all_results.append({"values": e.values, "token_count": token_count})
        time.sleep(0.5)  # 레이트리밋 여유

    return all_results


def insert_document(conn, source: dict) -> int:
    cur = conn.cursor()
    cur.execute(
        """
        INSERT INTO KB_DOCUMENT (title, doc_type, publisher, authors, published_year, source_url, file_path)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        RETURNING kb_doc_id
        """,
        (
            source["title"], source["doc_type"], source.get("publisher"),
            source.get("authors"), source.get("published_year"),
            source.get("source_url"), source.get("file_path"),
        ),
    )
    kb_doc_id = cur.fetchone()[0]
    conn.commit()
    cur.close()
    return kb_doc_id


def ingest_source(conn, source: dict):
    print(f"\n처리 중: {source['title']}")
    print(f"  다운로드: {source['source_url']}")

    try:
        pages = fetch_pdf_pages(source["source_url"])
    except Exception as e:
        print(f"  실패: {e}")
        return 0

    print(f"  페이지 수: {len(pages)}")

    kb_doc_id = insert_document(conn, source)
    print(f"  KB_DOCUMENT 저장 완료 (kb_doc_id={kb_doc_id})")

    # 페이지별로 청크를 만들되, 섹션/페이지번호를 같이 기록
    all_chunks = []  # [(chunk_text, page_number, section), ...]
    current_section = None
    for page_num, page_text in pages:
        current_section = detect_section(page_text, current_section)
        for c in chunk_text(page_text):
            all_chunks.append((c, page_num, current_section))

    print(f"  청크 {len(all_chunks)}개 생성")
    if not all_chunks:
        return 0

    texts_only = [c[0] for c in all_chunks]
    embeddings = embed_texts(texts_only)

    cur = conn.cursor()
    for idx, ((chunk_txt, page_num, section), emb) in enumerate(zip(all_chunks, embeddings)):
        cur.execute(
            """
            INSERT INTO KB_CHUNK (kb_doc_id, chunk_order, page_number, section, chunk_text, embedding, token_count)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            """,
            (kb_doc_id, idx, page_num, section, chunk_txt, emb["values"], emb["token_count"]),
        )
    conn.commit()
    cur.close()

    print(f"  KB_CHUNK 저장 완료: {len(all_chunks)}건")
    return len(all_chunks)


def main():
    database_url = os.environ.get("DATABASE_URL")
    if not database_url:
        print("DATABASE_URL 환경변수가 없습니다. .env에 추가해주세요.")
        return

    conn = psycopg2.connect(database_url)
    register_vector(conn)

    total = 0
    for source in SOURCES:
        total += ingest_source(conn, source)

    conn.close()
    print(f"\n전체 완료: 총 {total}개 청크 저장")


if __name__ == "__main__":
    main()
