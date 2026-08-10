-- Migration: CreateBatchSystemUser

-- 1. 배치 시스템 계정 생성
INSERT INTO tb_user (
                     id,
                     email,
                     name,
                     nickname,
                     password,
                     state,
                     academic_status,
                     profile_image_type,
                     is_v2,
                     is_email_verified,
                     report_count,
                     created_at,
                     updated_at
)
SELECT
    'system-batch-id',
    'SYSTEM_BATCH_ACCOUNT',
    '배치 시스템 계정',
    '시스템 배치',
    'LOGIN_DISABLED',
    'ACTIVE',
    'ENROLLED',
    'UNSET',
    true,
    false,
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM tb_user WHERE email = 'SYSTEM_BATCH_ACCOUNT'
);


-- 2. 배치 시스템 계정 권한 부여
INSERT INTO user_roles (user_id, role)
SELECT u.id, 'ADMIN'
FROM tb_user u
WHERE u.email = 'SYSTEM_BATCH_ACCOUNT'
    AND NOT EXISTS (
        SELECT 1
        FROM user_roles ur
        WHERE ur.user_id = u.id AND ur.role = 'ADMIN'
    );
