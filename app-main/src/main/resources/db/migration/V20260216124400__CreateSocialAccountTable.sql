-- Migration: CreateSocialAccountTable
CREATE TABLE tb_user_social_account
(
    id          VARCHAR(255) NOT NULL PRIMARY KEY,

    created_at  DATETIME(6) DEFAULT NULL,
    updated_at  DATETIME(6) DEFAULT NULL,

    social_id   VARCHAR(255) NOT NULL COMMENT '소셜 서비스의 식별자 (sub, id 등)',
    social_type VARCHAR(50)  NOT NULL COMMENT 'GOOGLE, KAKAO, APPLE',
    email       VARCHAR(255) NOT NULL COMMENT '소셜 계정의 이메일',

    user_id     VARCHAR(255) NOT NULL COMMENT '연동된 사용자 ID',

    CONSTRAINT chk_social_type
        CHECK (social_type IN ('GOOGLE', 'APPLE', 'KAKAO')),

    CONSTRAINT UK_social_type_and_id
        UNIQUE (social_id, social_type), -- 같은 소셜 타입 내 중복 방지

    CONSTRAINT FK_social_account_to_user
        FOREIGN KEY (user_id) REFERENCES tb_user (id)
);