-- Migration: MigrateAdmissionYearToInteger
-- tb_ceremony_target_admission_years.admission_year: VARCHAR(255) 2자리 → INT 4자리 변환
-- 72 이상이면 1900년대(ex. "72" → 1972), 미만이면 2000년대(ex. "19" → 2019)

-- Step 1: 기존 2자리 문자열을 4자리 연도 문자열로 업데이트
UPDATE tb_ceremony_target_admission_years
SET admission_year = CASE
    WHEN CAST(admission_year AS UNSIGNED) >= 72
        THEN CAST(1900 + CAST(admission_year AS UNSIGNED) AS CHAR)
    ELSE CAST(2000 + CAST(admission_year AS UNSIGNED) AS CHAR)
END
WHERE LENGTH(admission_year) = 2;

-- Step 2: 컬럼 타입을 INT로 변경
ALTER TABLE tb_ceremony_target_admission_years
    MODIFY COLUMN admission_year INT NOT NULL;
