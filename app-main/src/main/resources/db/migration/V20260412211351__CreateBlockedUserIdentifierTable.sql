-- Migration: CreateDroppedUserIdentifierTable

CREATE TABLE tb_dropped_user_identifier (
    id              VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id         VARCHAR(255) NOT NULL,
    identifier_type VARCHAR(30)  NOT NULL,
    identifier_hash VARCHAR(255) NOT NULL,
    reason          VARCHAR(255),
    created_at      DATETIME(6),
    updated_at      DATETIME(6),

    UNIQUE KEY uq_dropped_user_identifier (identifier_type, identifier_hash),
    KEY idx_dropped_user_identifier_user_id (user_id),
    CONSTRAINT fk_dropped_user_identifier_user
        FOREIGN KEY (user_id) REFERENCES tb_user(id)
);