CREATE TABLE tb_board_config (
    id varchar(255) NOT NULL,
    board_id VARCHAR(255) NOT NULL,
    is_anonymous BOOLEAN NOT NULL,
    read_scope VARCHAR(50) NOT NULL,
    write_scope VARCHAR(50) NOT NULL,
    is_notice BOOLEAN NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    created_at datetime(6) DEFAULT NULL,
    updated_at datetime(6) DEFAULT NULL
);

ALTER TABLE tb_board_config
ADD CONSTRAINT uq_board_id UNIQUE (board_id);
