-- Migration: CreateUserInterestTechAndDomainTable

-- tb_user_interest_tech 테이블 생성
CREATE TABLE tb_user_interest_tech (
    user_info_id VARCHAR(255) NOT NULL,
    interest_tech VARCHAR(50) NOT NULL,

    PRIMARY KEY (user_info_id, interest_tech),
    CONSTRAINT fk_user_interest_tech_user_info
        FOREIGN KEY (user_info_id) REFERENCES tb_user_info(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_interest_tech_interest_tech ON tb_user_interest_tech(interest_tech);


-- tb_user_interest_domain 테이블 생성
CREATE TABLE tb_user_interest_domain (
    user_info_id VARCHAR(255) NOT NULL,
    interest_domain VARCHAR(50) NOT NULL,

    PRIMARY KEY (user_info_id, interest_domain),
    CONSTRAINT fk_user_interest_domain_user_info
       FOREIGN KEY (user_info_id) REFERENCES tb_user_info(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_interest_domain_interest_domain ON tb_user_interest_domain(interest_domain);