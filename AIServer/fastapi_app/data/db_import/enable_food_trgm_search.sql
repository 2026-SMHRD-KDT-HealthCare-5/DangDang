-- food_info 테이블에 pg_trgm(문자열 유사도 검색) 확장을 켜고 인덱스를 만드는 스크립트.
-- load_food_table.sql로 food_info 테이블을 먼저 만든/데이터를 넣은 다음에 실행할 것.
--
-- psql / DBeaver / pgAdmin 아무 도구로나 이 파일 내용을 그대로 실행하면 됨.
-- 실행 권한이 있는 계정(DB_USERNAME)으로 접속해서 실행해야 함.

-- pg_trgm: 문자열을 3글자씩(trigram) 잘라서 비교해 유사도를 계산해주는 PostgreSQL 확장 기능.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- GIN 인덱스: food_name에 대해 유사도 검색(similarity())을 빠르게 하기 위한 인덱스.
-- 이 인덱스가 없어도 similarity()는 동작하지만, 테이블 전체를 매번 스캔하게 되어 느려짐.
CREATE INDEX IF NOT EXISTS idx_food_name_trgm ON food_info USING GIN (food_name gin_trgm_ops);

-- 확인용 쿼리 (실행 후 결과가 잘 나오는지 테스트해볼 것)
-- SELECT food_no, food_name, similarity(food_name, '치킨') AS score
-- FROM food_info
-- WHERE similarity(food_name, '치킨') > 0.15
-- ORDER BY score DESC
-- LIMIT 5;
