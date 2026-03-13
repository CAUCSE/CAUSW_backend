-- Migration: ModifyCeremonyTableDescriptionColumn

ALTER TABLE tb_ceremony MODIFY COLUMN description TEXT;

UPDATe tb_ceremony
SET ceremony_type =
    CASE
        WHEN ceremony_category in ('FUNERAL', 'ACCIDENT', 'ILLNESS') THEN 'CONDOLENCE'
        ELSE 'CELEBRATION'
    END
WHERE ceremony_type IS NULL;

UPDATE tb_ceremony
SET relation_type = 'ME' WHERE relation_type IS NULL;

UPDATE tb_ceremony
SET ceremony_custom_category = '기타' WHERE ceremony_custom_category IS NULL;