CREATE TABLE tb_event (
    id            VARCHAR(255) NOT NULL,
    title         VARCHAR(255) NOT NULL,
    landing_url   TEXT         NULL,
    is_active     BIT(1)       NOT NULL,
    display_order INT          NOT NULL,
    deleted_at    DATETIME(6)  NULL,
    created_at    DATETIME(6)  DEFAULT NULL,
    updated_at    DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE tb_event_attach_image_uuid_file (
    id           VARCHAR(255) NOT NULL,
    event_id     VARCHAR(255) NOT NULL,
    uuid_file_id VARCHAR(255) NOT NULL,
    created_at   DATETIME(6)  DEFAULT NULL,
    updated_at   DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_event_attach_image_event UNIQUE (event_id),
    CONSTRAINT uq_event_attach_image_uuid_file UNIQUE (uuid_file_id),
    CONSTRAINT fk_event_attach_image_event
        FOREIGN KEY (event_id) REFERENCES tb_event (id),
    CONSTRAINT fk_event_attach_image_uuid_file
        FOREIGN KEY (uuid_file_id) REFERENCES tb_uuid_file (id)
);
