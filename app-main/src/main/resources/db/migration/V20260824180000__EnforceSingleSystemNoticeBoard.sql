ALTER TABLE tb_board_config
    ADD COLUMN system_notice_unique_key TINYINT
        GENERATED ALWAYS AS (CASE WHEN is_system_notice = TRUE THEN 1 ELSE NULL END) STORED,
    ADD CONSTRAINT uq_board_config_single_system_notice UNIQUE (system_notice_unique_key);
