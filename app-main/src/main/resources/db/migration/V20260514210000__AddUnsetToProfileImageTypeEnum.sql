-- Migration: AddUnsetToProfileImageTypeEnum
-- profile_image_type ENUM에 UNSET 추가, 신규 유저 기본값을 UNSET으로 변경
ALTER TABLE tb_user
    MODIFY COLUMN profile_image_type
    ENUM('UNSET', 'MALE_1', 'MALE_2', 'FEMALE_1', 'FEMALE_2', 'CUSTOM', 'GHOST')
    NOT NULL DEFAULT 'UNSET';

-- 기존 MALE_1(미설정 기본값) → UNSET
-- v2 프로필 설정 API 미사용 전제: MALE_1은 모두 기본값 상태
UPDATE tb_user
SET profile_image_type = 'UNSET'
WHERE profile_image_type = 'MALE_1';
