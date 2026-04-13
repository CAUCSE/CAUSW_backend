-- Migration: MigrateCeremonySubscribeYearToInteger
-- tb_ceremony_subscribe_year.admission_year: VARCHAR(255) 2자리 → INT 4자리 변환
-- 72 이상이면 1900년대(ex. "72" → 1972), 미만이면 2000년대(ex. "19" → 2019)

-- Step 1: 기존 2자리 문자열을 4자리 연도 문자열로 업데이트
-- 1. 새 컬럼 추가
ALTER TABLE tb_ceremony_subscribe_year
    ADD COLUMN admission_year_new INT NULL;

-- 2. 2자리 데이터 이관
UPDATE tb_ceremony_subscribe_year
SET admission_year_new = CASE
    WHEN CAST(admission_year AS UNSIGNED) >= 72
        THEN 1900 + CAST(admission_year AS UNSIGNED)
    ELSE 2000 + CAST(admission_year AS UNSIGNED)
    END
WHERE admission_year IS NOT NULL
  AND LENGTH(admission_year) = 2;

-- 3. 혹시 이미 4자리 문자열 데이터가 있으면 같이 이관
UPDATE tb_ceremony_subscribe_year
SET admission_year_new = CAST(admission_year AS UNSIGNED)
WHERE admission_year IS NOT NULL
  AND LENGTH(admission_year) = 4;

-- 4. 값 검증 후 기존 컬럼 제거
ALTER TABLE tb_ceremony_subscribe_year
DROP
COLUMN admission_year;

-- 5. 새 컬럼 이름 변경
ALTER TABLE tb_ceremony_subscribe_year
    CHANGE COLUMN admission_year_new admission_year INT NOT NULL;
