-- pgvector 확장 활성화 (최초 1회)
CREATE EXTENSION IF NOT EXISTS vector;

-- 지식문서 테이블: RAG 검색에 쓸 논문/진료지침 원본 문서의 메타데이터
CREATE TABLE IF NOT EXISTS KB_DOCUMENT (
    kb_doc_id      SERIAL PRIMARY KEY,
    title          VARCHAR(300) NOT NULL,
    doc_type       VARCHAR(20)  NOT NULL,   -- '논문' | '진료지침'
    publisher      VARCHAR(100),
    authors        VARCHAR(200),
    published_year INT,
    source_url     VARCHAR(300),
    file_path      VARCHAR(300),
    created_at     TIMESTAMP DEFAULT now()
);

-- 지식청크 테이블: 문서를 청크 단위로 분할해 임베딩 벡터와 함께 저장, RAG 검색의 실제 대상
CREATE TABLE IF NOT EXISTS KB_CHUNK (
    chunk_id     SERIAL PRIMARY KEY,
    kb_doc_id    INT NOT NULL REFERENCES KB_DOCUMENT(kb_doc_id),
    chunk_order  INT NOT NULL,
    page_number  INT,
    section      VARCHAR(50),
    chunk_text   VARCHAR(1000) NOT NULL,
    embedding    VECTOR(1536),   -- gemini-embedding-001을 1536차원으로 맞춰서 사용
    token_count  INT
);

-- 벡터 유사도 검색 인덱스 (코사인 거리 기준)
CREATE INDEX IF NOT EXISTS idx_kb_chunk_embedding
    ON KB_CHUNK USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX IF NOT EXISTS idx_kb_chunk_doc_id ON KB_CHUNK (kb_doc_id);

-- 검증
-- SELECT count(*) FROM KB_DOCUMENT;
-- SELECT count(*) FROM KB_CHUNK;
