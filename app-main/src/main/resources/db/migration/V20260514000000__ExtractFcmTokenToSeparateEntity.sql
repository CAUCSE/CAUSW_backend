-- FCM 토큰을 별도 엔티티 테이블로 분리
-- 기존 tb_user_fcm_token(ElementCollection) → tb_fcm_token(Entity, token_value UNIQUE)
-- 동일 토큰 값이 여러 유저에 중복된 경우 첫 번째 user_id만 유지

CREATE TABLE tb_fcm_token
(
    id          VARCHAR(255) NOT NULL,
    user_id     VARCHAR(255) NOT NULL,
    token_value VARCHAR(255) NOT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT NOW(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT NOW(6),
    CONSTRAINT pk_fcm_token PRIMARY KEY (id),
    CONSTRAINT uk_fcm_token_value UNIQUE (token_value),
    INDEX idx_fcm_token_user_id (user_id),
    CONSTRAINT fk_fcm_token_user FOREIGN KEY (user_id) REFERENCES tb_user (id)
);

-- 기존 데이터 이전 (token_value 중복 시 MIN(user_id) 기준으로 1건만 유지)
INSERT INTO tb_fcm_token (id, user_id, token_value, created_at, updated_at)
SELECT UUID(), MIN(user_id), fcm_token_value, NOW(), NOW()
FROM tb_user_fcm_token
GROUP BY fcm_token_value;

DROP TABLE tb_user_fcm_token;
