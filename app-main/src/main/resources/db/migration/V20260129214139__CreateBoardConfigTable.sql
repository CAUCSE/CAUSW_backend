CREATE TABLE tb_board_config (
    id varchar(36) NOT NULL PRIMARY KEY,
    board_id VARCHAR(36) NOT NULL,
    is_anonymous BOOLEAN NOT NULL,
    read_scope VARCHAR(50) NOT NULL,
    write_scope VARCHAR(50) NOT NULL,
    is_notice BOOLEAN NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    display_order INT DEFAULT 0,
    created_at datetime(6) DEFAULT NULL,
    updated_at datetime(6) DEFAULT NULL
);

ALTER TABLE tb_board_config
ADD CONSTRAINT uq_board_id UNIQUE (board_id);

ALTER TABLE tb_board_config
    ADD CONSTRAINT chk_read_scope
        CHECK (read_scope IN ('ENROLLED', 'GRADUATED', 'BOTH'));

ALTER TABLE tb_board_config
    ADD CONSTRAINT chk_write_scope
        CHECK (write_scope IN ('ONLY_ADMIN', 'ALL_USER'));

ALTER TABLE tb_board_config
    ADD CONSTRAINT chk_visibility
        CHECK (visibility IN ('VISIBLE', 'HIDDEN'));