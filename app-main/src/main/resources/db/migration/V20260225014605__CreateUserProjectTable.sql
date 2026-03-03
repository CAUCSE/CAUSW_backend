-- Migration: CreateUserProjectTable

-- tb_user_project 테이블 생성
CREATE TABLE tb_user_project (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,

    user_info_id VARCHAR(255) NOT NULL,
    start_year INT NOT NULL,
    start_month INT NOT NULL,
    end_year INT NULL,
    end_month INT NULL,
    description VARCHAR(50) NOT NULL,

    CONSTRAINT fk_user_project_user_info_id
        FOREIGN KEY (user_info_id) REFERENCES tb_user_info(id)
);