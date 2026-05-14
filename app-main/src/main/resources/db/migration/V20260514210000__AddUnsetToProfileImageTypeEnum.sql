-- Migration: AddUnsetToProfileImageTypeEnum
-- profile_image_type ENUM에 UNSET 추가, 신규 유저 기본값을 UNSET으로 변경
ALTER TABLE tb_user
    MODIFY COLUMN profile_image_type
    ENUM('UNSET', 'MALE_1', 'MALE_2', 'FEMALE_1', 'FEMALE_2', 'CUSTOM', 'GHOST')
    NOT NULL DEFAULT 'UNSET';
