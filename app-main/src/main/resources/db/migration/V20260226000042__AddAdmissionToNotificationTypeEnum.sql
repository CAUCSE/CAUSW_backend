-- 재학인증 알림(ADMISSION) 저장을 위해 notice_type ENUM 값을 확장한다.
-- 기존 데이터는 유지되며, 컬럼 정의만 갱신된다.
ALTER TABLE tb_notification
    MODIFY COLUMN notice_type ENUM('POST', 'COMMENT', 'CEREMONY', 'BOARD', 'ADMISSION') NULL DEFAULT NULL;
