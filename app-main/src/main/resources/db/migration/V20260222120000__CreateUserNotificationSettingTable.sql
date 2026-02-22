CREATE TABLE tb_user_notification_setting (
    id          VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at  DATETIME(6)  NULL,
    updated_at  DATETIME(6)  NULL,
    user_id     VARCHAR(255) NOT NULL,
    setting_key VARCHAR(100) NOT NULL,
    enabled     TINYINT(1)   NOT NULL,
    CONSTRAINT uq_user_setting UNIQUE (user_id, setting_key),
    CONSTRAINT fk_user_setting_user FOREIGN KEY (user_id) REFERENCES tb_user (id)
);

CREATE INDEX idx_setting_user ON tb_user_notification_setting (user_id);
