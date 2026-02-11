-- Migration: ModifyAdmissionYearColumnInUserNullable

ALTER TABLE tb_user
MODIFY COLUMN admission_year INT NULL;