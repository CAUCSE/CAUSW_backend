-- Migration: CreateUserInfo

CREATE TABLE tb_user_info (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,

    user_id VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(200) NULL,
    job VARCHAR(50) NULL,
    github_link VARCHAR(255) NULL,
    linkedin_link VARCHAR(255) NULL,
    instagram_link VARCHAR(255) NULL,
    notion_link VARCHAR(255) NULL,
    velog_link VARCHAR(255) NULL,
    is_phone_number_visible BIT(1) NULL DEFAULT b'0',

    CONSTRAINT fk_user_info_user_id
        FOREIGN KEY (user_id) REFERENCES tb_user(id)
);
