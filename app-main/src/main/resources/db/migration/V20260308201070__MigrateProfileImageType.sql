-- Migration: MigrateProfileImageTypeForExistingUsers
-- 1. profile_image_type ENUM에 GHOST 값 추가
ALTER TABLE tb_user
    MODIFY COLUMN profile_image_type ENUM ('MALE_1', 'MALE_2', 'FEMALE_1', 'FEMALE_2', 'CUSTOM', 'GHOST') NOT NULL DEFAULT 'MALE_1';

-- 2. 기존 유저 마이그레이션:
--    - tb_user_profile_uuid_file(커스텀 이미지)가 연결된 유저 → CUSTOM
--    - 그 외 유저 → MALE_1 (이미 기본값이지만 명시적으로 설정)
UPDATE tb_user u
SET u.profile_image_type = CASE
                               WHEN EXISTS (
                                   SELECT 1 FROM tb_user_profile_uuid_file upi WHERE upi.user_id = u.id
                               ) THEN 'CUSTOM'
                               ELSE 'MALE_1'
    END
WHERE u.profile_image_type = 'MALE_1';

