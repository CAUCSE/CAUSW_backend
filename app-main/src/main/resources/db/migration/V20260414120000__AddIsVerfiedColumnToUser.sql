-- Migration: AddIsVerfiedColumnToUser

ALTER TABLE tb_user
    ADD COLUMN is_email_verified TINYINT(1) NOT NULL DEFAULT 0 COMMENT '이메일 인증 여부';
