-- Migration: ModifyCeremonyTable

-- 1. ceremony_type 칼럼 추가
ALTER TABLE tb_ceremony
    ADD COLUMN ceremony_type ENUM('CELEBRATION', 'CONDOLENCE') NULL;

-- 2. 기존 row의 ceremony_type은 임의 값으로 채움 - CELEBRATION
--    기존 ceremony_category가 경사인 row의 ceremony_type을 CELEBRATION으로 채움
--    그 외의 경우는 CONDOLENCE로 채움
UPDATE tb_ceremony
SET ceremony_type =
    CASE
        WHEN ceremony_category IN ('MARRIAGE', 'GRADUATION', 'ETC') THEN 'CELEBRATION'
        ELSE 'CONDOLENCE'
    END
WHERE ceremony_type IS NULL;

-- 3. ceremony_type에 NOT NULL 적용 / description, ceremony_category, end_date에 NULL 적용
ALTER TABLE tb_ceremony
    MODIFY ceremony_type ENUM('CELEBRATION', 'CONDOLENCE') NOT NULL,
    MODIFY description VARCHAR(255) NULL,
    MODIFY ceremony_category VARCHAR(31) NULL,
    MODIFY end_date DATE NULL;

-- 4. ceremony_category ENUM에서 String으로 변경
ALTER TABLE tb_ceremony
    MODIFY ceremony_category VARCHAR(31) NOT NULL;

-- 5. start_time, end_time,
--    relation_type, family_relation, alumni_relation, alumni_name, alumni_admission_year,
--    address, postal_address, detailed_address, contact, link 칼럼 추가
ALTER TABLE tb_ceremony
    ADD COLUMN start_time TIME NULL,
    ADD COLUMN end_time TIME NULL,
    ADD COLUMN relation_type ENUM('ME', 'FAMILY', 'ALUMNI') NULL,
    ADD COLUMN family_relation ENUM('SPOUSE', 'FATHER', 'MOTHER', 'FATHER_IN_LAW', 'MOTHER_IN_LAW', 'SON', 'DAUGHTER', 'BROTHERS', 'SISTERS', 'SIBLINGS', 'GRANDFATHER', 'GRANDMOTHER') NULL,
    ADD COLUMN alumni_relation ENUM('ALUMNI', 'SPOUSE', 'FATHER', 'MOTHER', 'FATHER_IN_LAW', 'MOTHER_IN_LAW', 'SON', 'DAUGHTER') NULL,
    ADD COLUMN alumni_name VARCHAR(20) NULL,
    ADD COLUMN alumni_admission_year VARCHAR(5) NULL,
    ADD COLUMN address VARCHAR(50) NULL,
    ADD COLUMN postal_address VARCHAR(10) NULL,
    ADD COLUMN detailed_address VARCHAR(80) NULL,
    ADD COLUMN contact VARCHAR(20) NULL,
    ADD COLUMN link VARCHAR(255) NULL;

-- 6. relation_type 기존 데이터 임의로 채우기
UPDATE tb_ceremony SET relation_type = 'ME' WHERE relation_type IS NULL;
ALTER TABLE tb_ceremony MODIFY relation_type ENUM('ME', 'FAMILY', 'ALUMNI') NOT NULL;