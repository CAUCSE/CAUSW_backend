CREATE TABLE tb_terms (
    id                VARCHAR(255) NOT NULL PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    effective_date    DATE         NOT NULL,
    last_revised_date DATE         NOT NULL,
    content           TEXT         NOT NULL,
    created_at        DATETIME(6),
    updated_at        DATETIME(6)
);
