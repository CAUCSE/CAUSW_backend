CREATE TABLE tb_board_config_department (
    board_config_id BIGINT NOT NULL,
    department      VARCHAR(50) NOT NULL,
    PRIMARY KEY (board_config_id, department),
    CONSTRAINT fk_board_config_dept
        FOREIGN KEY (board_config_id) REFERENCES tb_board_config (id) ON DELETE CASCADE
);
