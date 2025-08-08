-- Migration: CreateDefaultCeremonyNotificationSettings

INSERT INTO TB_CEREMONY_PUSH_NOTIFICATION (
    id,
    user_id,
    is_notification_active,
    is_set_all,
    created_at,
    updated_at
)
SELECT
    UUID() as id,
    u.id as user_id,
    true as is_notification_active,
    true as is_set_all,
    NOW() as created_at,
    NOW() as updated_at
FROM tb_user u
WHERE u.academic_status IN ('ENROLLED', 'GRADUATED', 'LEAVE_OF_ABSENCE')
  AND NOT EXISTS (
    SELECT 1 FROM TB_CEREMONY_PUSH_NOTIFICATION cpn
    WHERE cpn.user_id = u.id
);