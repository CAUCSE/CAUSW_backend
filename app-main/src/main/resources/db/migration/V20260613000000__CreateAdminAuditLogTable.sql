-- Migration: CreateAdminAuditLogTable
-- 공통 관리자 감사 로그 테이블 생성 및 기존 사용자 관리 액션 로그 백필

CREATE TABLE tb_admin_audit_log (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,

    category VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_description VARCHAR(255) NOT NULL,

    actor_user_id VARCHAR(255) NOT NULL,
    actor_email VARCHAR(255) NOT NULL,
    actor_name VARCHAR(255) NULL,
    actor_student_id VARCHAR(255) NULL,

    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    target_email VARCHAR(255) NULL,
    target_name VARCHAR(255) NULL,
    target_student_id VARCHAR(255) NULL,

    summary VARCHAR(500) NOT NULL,
    metadata_json TEXT NULL,

    KEY idx_admin_audit_log_created_at (created_at),
    KEY idx_admin_audit_log_category_action_created (category, action_type, created_at),
    KEY idx_admin_audit_log_actor_email_created (actor_email, created_at),
    KEY idx_admin_audit_log_actor_name_created (actor_name, created_at),
    KEY idx_admin_audit_log_actor_student_id_created (actor_student_id, created_at),
    KEY idx_admin_audit_log_target_email_created (target_email, created_at),
    KEY idx_admin_audit_log_target_name_created (target_name, created_at),
    KEY idx_admin_audit_log_target_student_id_created (target_student_id, created_at)
);

INSERT INTO tb_admin_audit_log (
    id,
    created_at,
    updated_at,
    category,
    action_type,
    action_description,
    actor_user_id,
    actor_email,
    actor_name,
    actor_student_id,
    target_type,
    target_id,
    target_email,
    target_name,
    target_student_id,
    summary,
    metadata_json
)
SELECT
    log.id,
    log.created_at,
    log.updated_at,
    'USER',
    log.action_type,
    CASE log.action_type
        WHEN 'DROP' THEN '유저 추방'
        WHEN 'RESTORE' THEN '추방 유저 복구'
        WHEN 'ROLE_CHANGE' THEN '유저 역할 변경'
        ELSE log.action_type
    END,
    log.admin_user_id,
    log.admin_user_email,
    actor.name,
    actor.student_id,
    'USER',
    log.target_user_id,
    log.target_user_email,
    target.name,
    target.student_id,
    CASE log.action_type
        WHEN 'DROP' THEN CONCAT(log.admin_user_email, ' dropped user ', log.target_user_email)
        WHEN 'RESTORE' THEN CONCAT(log.admin_user_email, ' restored user ', log.target_user_email)
        WHEN 'ROLE_CHANGE' THEN CONCAT(log.admin_user_email, ' changed roles for user ', log.target_user_email)
        ELSE CONCAT(log.admin_user_email, ' performed ', log.action_type, ' on user ', log.target_user_email)
    END,
    JSON_OBJECT(
        'beforeState', log.before_state,
        'afterState', log.after_state,
        'beforeRoles', log.before_roles,
        'afterRoles', log.after_roles,
        'reason', log.reason
    )
FROM tb_user_admin_action_log log
LEFT JOIN tb_user actor ON actor.id = log.admin_user_id
LEFT JOIN tb_user target ON target.id = log.target_user_id;
