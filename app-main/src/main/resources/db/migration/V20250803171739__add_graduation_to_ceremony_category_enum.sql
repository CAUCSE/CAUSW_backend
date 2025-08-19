-- Migration: add_graduation_to_ceremony_category_enum

ALTER TABLE tb_ceremony
MODIFY COLUMN ceremony_category ENUM('MARRIAGE','FUNERAL','GRADUATION','ETC') NOT NULL;