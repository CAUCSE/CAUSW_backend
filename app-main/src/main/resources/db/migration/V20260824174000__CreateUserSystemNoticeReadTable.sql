CREATE TABLE tb_user_system_notice_read (
    user_id           VARCHAR(255) NOT NULL,
    last_read_post_id VARCHAR(255) NULL,
    created_at        DATETIME(6) DEFAULT NULL,
    updated_at        DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_system_notice_read_user
        FOREIGN KEY (user_id) REFERENCES tb_user (id),
    CONSTRAINT fk_user_system_notice_read_post
        FOREIGN KEY (last_read_post_id) REFERENCES tb_post (id)
);
