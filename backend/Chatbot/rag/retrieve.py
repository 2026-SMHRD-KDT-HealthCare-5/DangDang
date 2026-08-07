# -*- coding: utf-8 -*-
"""
RAG 검색 모듈 (KB_DOCUMENT / KB_CHUNK 스키마 기준)

사용자 질문을 임베딩해서 KB_CHUNK에서 코사인 유사도가 가장 높은 청크들을 가져오고,
KB_DOCUMENT와 조인해서 출처(제목/저자/링크)까지 함께 반환한다.
"""

import os
import psycopg2
from pgvector.psycopg2 import register_vector
from dotenv import load_dotenv
from google import genai
from google.genai import types

load_dotenv()

client = genai.Client(api_key=os.environ.get("GEMINI_API_KEY"))
EMBEDDING_MODEL = "gemini-embedding-001"
EMBEDDING_DIM = 1536  # ingest.py, schema.sql의 VECTOR(1536)과 반드시 일치해야 함


def embed_query(query: str) -> list[float]:
    response = client.models.embed_content(
        model=EMBEDDING_MODEL,
        contents=query,
        config=types.EmbedContentConfig(
            task_type="RETRIEVAL_QUERY",
            output_dimensionality=EMBEDDING_DIM,
        ),
    )
    return response.embeddings[0].values


def search_knowledge(query: str, top_k: int = 4, min_similarity: float = 0.5) -> list[dict]:
    """
    질문과 유사한 문서 청크를 top_k개 반환.
    반환: [{chunk_text, title, authors, source_url, page_number, section, similarity}, ...]
    similarity가 min_similarity보다 낮으면 관련성 없다고 보고 제외.
    """
    database_url = os.environ.get("DATABASE_URL")
    if not database_url:
        return []

    query_vec = embed_query(query)

    conn = psycopg2.connect(database_url)
    register_vector(conn)
    cur = conn.cursor()

    # <=> 는 pgvector의 코사인 거리(distance) 연산자. similarity = 1 - distance
    cur.execute(
        """
        SELECT
            c.chunk_text, c.page_number, c.section,
            d.title, d.authors, d.source_url,
            1 - (c.embedding <=> %s) AS similarity
        FROM KB_CHUNK c
        JOIN KB_DOCUMENT d ON c.kb_doc_id = d.kb_doc_id
        ORDER BY c.embedding <=> %s
        LIMIT %s
        """,
        (query_vec, query_vec, top_k),
    )
    rows = cur.fetchall()
    cur.close()
    conn.close()

    results = [
        {
            "chunk_text": r[0],
            "page_number": r[1],
            "section": r[2],
            "title": r[3],
            "authors": r[4],
            "source_url": r[5],
            "similarity": float(r[6]),
        }
        for r in rows
        if r[6] >= min_similarity
    ]
    return results


if __name__ == "__main__":
    import sys
    query = sys.argv[1] if len(sys.argv) > 1 else "당뇨병 환자는 운동을 얼마나 해야 하나요?"
    results = search_knowledge(query)
    print(f"검색어: {query}\n")
    for r in results:
        print(f"[{r['similarity']:.3f}] {r['title']} (p.{r['page_number']}, {r['section']})")
        print(f"  {r['chunk_text'][:150]}...")
        print()
