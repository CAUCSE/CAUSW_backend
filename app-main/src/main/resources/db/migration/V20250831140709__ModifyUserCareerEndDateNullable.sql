-- Migration: ModifyUserCareerEndDateNullable

ALTER TABLE tb_user_career
    MODIFY COLUMN end_year INT NULL,MODIFY COLUMN end_month INT NULL;