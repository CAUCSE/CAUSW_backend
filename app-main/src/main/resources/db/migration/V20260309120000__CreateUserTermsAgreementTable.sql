CREATE TABLE tb_user_terms_agreement (
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id    VARCHAR(255) NOT NULL,
    terms_id   VARCHAR(255) NOT NULL,
    agreed_at  DATETIME(6)  NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    UNIQUE KEY uq_user_terms_agreement (user_id, terms_id),
    CONSTRAINT fk_uta_user  FOREIGN KEY (user_id)  REFERENCES tb_user(id),
    CONSTRAINT fk_uta_terms FOREIGN KEY (terms_id) REFERENCES tb_terms(id)
);
