-- Migration: ModifyUserInfoTable

-- is_message_visible 칼럼 추가
-- 기존 is_phone_number_visible에 맞춰서 기본값 설정됨
ALTER TABLE tb_user_info
    ADD COLUMN is_message_visible BIT(1) NULL AFTER is_phone_number_visible;

UPDATE tb_user_info
SET is_message_visible = is_phone_number_visible
WHERE is_message_visible IS NULL;
