-- Migration: DropColumnInUserFcmToken

ALTER TABLE tb_user_fcm_token
DROP COLUMN fcm_token;