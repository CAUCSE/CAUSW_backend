-- Migration: AddProfileImageTypeColumnToUser
-- profile_image_type 컬럼 추가 (기본값: MALE_1)
ALTER TABLE tb_user
    ADD COLUMN profile_image_type ENUM ('MALE_1', 'MALE_2', 'FEMALE_1', 'FEMALE_2', 'CUSTOM') NOT NULL DEFAULT 'MALE_1';
