CREATE TABLE tb_event (
    id            VARCHAR(255) NOT NULL,
    title         VARCHAR(255) NOT NULL,
    landing_url   TEXT         NOT NULL,
    is_active     BIT(1)       NOT NULL,
    display_order INT          NOT NULL,
    deleted_at    DATETIME(6)  NULL,
    created_at    DATETIME(6)  DEFAULT NULL,
    updated_at    DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (id)
);
