-- Migration: CreateUserAdminActionLogTable
-- 관리자 사용자 액션(추방/복구/권한변경) 감사 로그 테이블 생성
CREATE TABLE tb_user_admin_action_log (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,

    admin_user_id VARCHAR(255) NOT NULL,
    admin_user_email VARCHAR(255) NOT NULL,
    target_user_id VARCHAR(255) NOT NULL,
    target_user_email VARCHAR(255) NOT NULL,

    action_type VARCHAR(50) NOT NULL,
    before_state VARCHAR(50) NULL,
    after_state VARCHAR(50) NULL,
    before_roles VARCHAR(255) NULL,
    after_roles VARCHAR(255) NULL,
    reason VARCHAR(255) NULL
);
