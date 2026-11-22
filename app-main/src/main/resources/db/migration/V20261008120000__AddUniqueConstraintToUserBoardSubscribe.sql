-- 게시판 알림 구독 설정의 (user_id, board_id) 중복 저장을 방지한다.
-- 적용 전 운영 데이터 확인 결과: 중복 0건, NULL 0건 (2026-10-06)
ALTER TABLE tb_user_board_subscribe
    MODIFY COLUMN user_id VARCHAR(255) NOT NULL,
    MODIFY COLUMN board_id VARCHAR(255) NOT NULL,
    MODIFY COLUMN is_subscribed BIT(1) NOT NULL,
    ADD CONSTRAINT uk_user_board_subscribe_user_board UNIQUE (user_id, board_id);
