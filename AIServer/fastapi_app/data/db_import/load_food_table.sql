-- food_info 테이블 생성 (스크린샷 스키마 기준)
CREATE TABLE IF NOT EXISTS food_info (
    food_no       INTEGER PRIMARY KEY,
    food_name     VARCHAR(100) NOT NULL,
    calorie       NUMERIC(6,2),
    carb          NUMERIC(6,2),
    protein       NUMERIC(6,2),
    fat           NUMERIC(6,2),
    fiber         NUMERIC(6,2),
    serving_size  INTEGER,
    sugar         NUMERIC(6,2)
);

-- food_for_db.csv 데이터 적재
-- psql에서 실행하는 경우 (파일 경로는 실제 위치로 수정):
COPY food_info (food_no, food_name, calorie, carb, protein, fat, fiber, serving_size, sugar)
FROM '/절대/경로/food_for_db.csv'
WITH (FORMAT csv, HEADER true, ENCODING 'UTF8');

-- 검증
SELECT count(*) FROM food_info;
SELECT * FROM food_info LIMIT 5;

-- 음식명 검색 성능을 위한 인덱스 (rapidfuzz 매칭 전에 후보군 좁힐 때 유용)
CREATE INDEX IF NOT EXISTS idx_food_name ON food_info (food_name);
