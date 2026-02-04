-- Migration: ModifyCeremonyTable

-- 1. ceremony_type, ceremony_custom_category 칼럼 추가
ALTER TABLE tb_ceremony
    ADD COLUMN ceremony_type ENUM('CELEBRATION', 'CONDOLENCE') NULL AFTER updated_at,
    ADD COLUMN ceremony_custom_category VARCHAR(31) NULL AFTER ceremony_category;

-- 2. 기존 row의 ceremony_type은 임의 값으로 채움 - CELEBRATION
--    기존 ceremony_category가 경사인 row의 ceremony_type을 CELEBRATION으로 채움
--    그 외의 경우는 CONDOLENCE로 채움
UPDATE tb_ceremony
SET ceremony_type =
    CASE
        WHEN ceremony_category IN ('MARRIAGE', 'GRADUATION', 'OPENING', 'BIRTHDAY', 'ETC') THEN 'CELEBRATION'
        ELSE 'CONDOLENCE'
    END
WHERE ceremony_type IS NULL;

-- 3. ceremony_type, ceremony_category에 NOT NULL, description, end_date에 NULL 적용
ALTER TABLE tb_ceremony
    MODIFY ceremony_type ENUM('CELEBRATION', 'CONDOLENCE') NOT NULL,
    MODIFY description VARCHAR(255) NULL,
    MODIFY ceremony_category ENUM('MARRIAGE', 'FIRST_BIRTHDAY', 'OPENING', 'BIRTHDAY',
        'GRADUATION', 'FUNERAL', 'ACCIDENT', 'ILLNESS', 'ETC') NOT NULL,
    MODIFY end_date DATE NULL;

-- 5. start_time, end_time,
--    relation_type, family_relation, alumni_relation, alumni_name, alumni_admission_year,
--    address, postal_address, detailed_address, contact, link 칼럼 추가
ALTER TABLE tb_ceremony
    ADD COLUMN end_time TIME NULL AFTER start_date,
    ADD COLUMN start_time TIME NULL AFTER end_time,
    ADD COLUMN relation_type ENUM('ME', 'FAMILY', 'ALUMNI') NULL AFTER user_id,
    ADD COLUMN family_relation ENUM('SPOUSE', 'FATHER', 'MOTHER', 'FATHER_IN_LAW', 'MOTHER_IN_LAW', 'SON', 'DAUGHTER',
        'BROTHERS', 'SISTERS', 'SIBLINGS', 'GRANDFATHER', 'GRANDMOTHER') NULL AFTER relation_type,
    ADD COLUMN alumni_relation ENUM('ALUMNI', 'SPOUSE', 'FATHER', 'MOTHER', 'FATHER_IN_LAW', 'MOTHER_IN_LAW', 'SON',
        'DAUGHTER') NULL AFTER family_relation,
    ADD COLUMN alumni_name VARCHAR(20) NULL AFTER alumni_relation,
    ADD COLUMN alumni_admission_year VARCHAR(5) NULL AFTER alumni_name,
    ADD COLUMN address VARCHAR(50) NULL AFTER alumni_admission_year,
    ADD COLUMN postal_address VARCHAR(10) NULL AFTER address,
    ADD COLUMN detailed_address VARCHAR(80) NULL AFTER postal_address,
    ADD COLUMN contact VARCHAR(20) NULL AFTER detailed_address,
    ADD COLUMN link VARCHAR(255) NULL AFTER contact;

-- 6. relation_type 기존 데이터 임의로 채우기
UPDATE tb_ceremony SET relation_type = 'ME' WHERE relation_type IS NULL;
ALTER TABLE tb_ceremony MODIFY relation_type ENUM('ME', 'FAMILY', 'ALUMNI') NOT NULL;