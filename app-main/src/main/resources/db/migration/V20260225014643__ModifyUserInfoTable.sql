-- Migration: ModifyUserInfoTable

-- is_message_visible 칼럼 추가
-- 기본 False
-- 기존에 is_phone_number_visible이 True이면 True로
ALTER TABLE tb_user_info
    ADD COLUMN is_message_visible BOOLEAN NULL AFTER is_phone_number_visible;

UPDATE tb_user_info
SET is_message_visible =
    IF (is_phone_number_visible, TRUE, FALSE)
WHERE is_message_visible IS NULL;

ALTER TABLE tb_user_info
    MODIFY is_message_visible BOOLEAN NOT NULL,
    MODIFY is_phone_number_visible BOOLEAN NOT NULL;
