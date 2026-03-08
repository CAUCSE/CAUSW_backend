-- Migration: ModifyCeremonyTable

ALTER TABLE tb_ceremony
    MODIFY COLUMN ceremony_type ENUM ('CELEBRATION', 'CONDOLENCE') NULL,
    MODIFY COLUMN ceremony_category ENUM (
        'MARRIAGE', 'FIRST_BIRTHDAY', 'OPENING', 'BIRTHDAY',
        'FUNERAL', 'ACCIDENT', 'ILLNESS',
        'ETC', 'GRADUATION') NULL,
    MODIFY COLUMN relation_type ENUM ('ME', 'FAMILY', 'INSTEAD') NULL;