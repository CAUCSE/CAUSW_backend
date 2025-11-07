-- Migration: CreateUserCareer

CREATE TABLE tb_user_career (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,

    user_info_id VARCHAR(255) NOT NULL,
    start_year INT NOT NULL,
    start_month INT NOT NULL,
    end_year INT NOT NULL,
    end_month INT NOT NULL,
    description VARCHAR(50) NOT NULL,

    CONSTRAINT fk_user_career_user_info_id
        FOREIGN KEY (user_info_id) REFERENCES tb_user_info(id)
);