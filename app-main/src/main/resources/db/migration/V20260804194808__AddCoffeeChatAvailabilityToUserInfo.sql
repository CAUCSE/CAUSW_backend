-- Migration: AddCoffeeChatAvailabilityToUserInfo

-- 커피챗 허용 boolean 추가
ALTER TABLE tb_user_info
    ADD COLUMN is_coffee_chat_available boolean not null default false;

-- 기존 전화번호 공개 null 값 default false로 변경
UPDATE tb_user_info
SET is_phone_number_visible = b'0'
WHERE is_phone_number_visible IS NULL;

ALTER TABLE tb_user_info
    MODIFY COLUMN is_phone_number_visible BIT(1) NOT NULL DEFAULT b'0';

