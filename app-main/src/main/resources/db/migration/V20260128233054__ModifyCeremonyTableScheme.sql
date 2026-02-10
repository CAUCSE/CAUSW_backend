-- Migration: ModifyCeremonyTable

-- 1. 새로운 칼럼 추가
--    ceremony_type, end_time, start_time, relation_type, family_relation, alumni_relation,
--    alumni_name, alumni_admission_year, address, postal_address, detailed_address,
--    contact, link
ALTER TABLE tb_ceremony
    ADD COLUMN ceremony_type ENUM('CELEBRATION', 'CONDOLENCE') NULL AFTER updated_at,
    ADD COLUMN ceremony_custom_category VARCHAR(50) NULL AFTER ceremony_category,
    ADD COLUMN end_time TIME NULL AFTER start_date,
    ADD COLUMN start_time TIME NULL AFTER end_time,
    ADD COLUMN relation_type ENUM('ME', 'FAMILY', 'INSTEAD') NULL AFTER user_id,
    ADD COLUMN family_relation VARCHAR(20) NULL AFTER relation_type,
    ADD COLUMN alumni_relation VARCHAR(20) NULL AFTER family_relation,
    ADD COLUMN alumni_name VARCHAR(20) NULL AFTER alumni_relation,
    ADD COLUMN alumni_admission_year VARCHAR(5) NULL AFTER alumni_name,
    ADD COLUMN address VARCHAR(40) NULL AFTER alumni_admission_year,
    ADD COLUMN postal_address VARCHAR(10) NULL AFTER address,
    ADD COLUMN detailed_address VARCHAR(30) NULL AFTER postal_address,
    ADD COLUMN contact VARCHAR(20) NULL AFTER detailed_address,
    ADD COLUMN link VARCHAR(50) NULL AFTER contact;


-- 2. 기존 데이터의 ceremony_type 채우기
UPDATE tb_ceremony
SET ceremony_type =
    CASE
        WHEN ceremony_category = 'FUNERAL' THEN 'CONDOLENCE'
        ELSE 'CELEBRATION'
    END
WHERE ceremony_type IS NULL;

-- 3. 기존 데이터의 relation_type 채우기
UPDATE tb_ceremony
SET relation_type = 'ME' WHERE relation_type IS NULL;

-- 4. 데이터 조건 설정
ALTER TABLE tb_ceremony
    MODIFY ceremony_type ENUM('CELEBRATION', 'CONDOLENCE') NOT NULL,
    MODIFY relation_type ENUM('ME', 'FAMILY', 'INSTEAD') NOT NULL;

-- 4.5 ceremony_category 정규화 + 한글 매핑

UPDATE tb_ceremony
SET
    ceremony_custom_category =
        CASE
            WHEN ceremony_category = 'GRADUATION' THEN '졸업식'
            WHEN ceremony_category = 'ETC' THEN '기타'
            ELSE ceremony_category
            END,
    ceremony_category = 'ETC'
WHERE ceremony_category NOT IN (
                                'MARRIAGE',
                                'FIRST_BIRTHDAY',
                                'OPENING',
                                'BIRTHDAY',
                                'FUNERAL',
                                'ACCIDENT',
                                'ILLNESS',
                                'ETC'
    )
   OR ceremony_category IN ('GRADUATION', 'ETC')
   OR ceremony_category IS NULL;

-- 5. NOT NULL로 변경
ALTER TABLE tb_ceremony
    MODIFY ceremony_category ENUM('MARRIAGE', 'FIRST_BIRTHDAY', 'OPENING', 'BIRTHDAY', 'FUNERAL', 'ACCIDENT', 'ILLNESS', 'ETC') NOT NULL,
    MODIFY description VARCHAR(255) NULL,
    MODIFY end_date DATE NULL;