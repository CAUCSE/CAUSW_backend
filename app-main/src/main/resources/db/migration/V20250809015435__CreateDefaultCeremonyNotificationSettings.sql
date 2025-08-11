-- Migration: CreateDefaultCeremonyNotificationSettings

INSERT INTO tb_ceremony_push_notification (
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
AND u.state = 'ACTIVE'
AND EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id
    AND ur.role != 'NONE'
)
AND NOT EXISTS (
    SELECT 1 FROM tb_ceremony_push_notification cpn
    WHERE cpn.user_id = u.id
);