CREATE TABLE tb_terms (
    id             VARCHAR(255) NOT NULL PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    type           VARCHAR(50)  NOT NULL,
    is_required    BOOLEAN      NOT NULL DEFAULT FALSE,
    version        INT          NOT NULL,
    effective_date DATE         NOT NULL,
    content        TEXT         NOT NULL,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    UNIQUE KEY uq_terms_type_version (type, version)
);
