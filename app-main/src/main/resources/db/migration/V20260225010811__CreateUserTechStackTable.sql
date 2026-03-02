-- Migration: CreateUserTechStackTable

-- tb_user_tech_stack 테이블 생성
CREATE TABLE tb_user_tech_stack (
    user_info_id VARCHAR(255) NOT NULL,
    tech_stack VARCHAR(50) NOT NULL,

    PRIMARY KEY (user_info_id, tech_stack),
    CONSTRAINT fk_user_tech_stack_user_info
        FOREIGN KEY (user_info_id) REFERENCES tb_user_info(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_tech_stack_tech_stack ON tb_user_tech_stack(tech_stack);