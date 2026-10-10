-- Migration: create_calendar_filter_preference

CREATE TABLE tb_calendar_filter_preference (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,

    user_id VARCHAR(255) NOT NULL UNIQUE,

    CONSTRAINT fk_calendar_filter_preference_user
        FOREIGN KEY (user_id) REFERENCES tb_user (id)
)
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE tb_calendar_filter_category (
    calendar_filter_preference_id VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,

    PRIMARY KEY (calendar_filter_preference_id, category),

    CONSTRAINT fk_calendar_filter_category_preference
        FOREIGN KEY (calendar_filter_preference_id)
            REFERENCES tb_calendar_filter_preference (id)
            ON DELETE CASCADE
)
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;