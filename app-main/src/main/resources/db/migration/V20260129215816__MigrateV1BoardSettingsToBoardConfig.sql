-- Migration: MigrateV1BoardSettingsToBoardConfig
INSERT INTO tb_board_config (
    board_id,
    is_anonymous,
    read_scope,
    write_scope,
    is_notice,
    visibility,
    display_order,
    created_at,
    updated_at
)
SELECT
    b.id,
    b.is_anonymous_allowed,
    CASE
        WHEN b.is_alumni = true THEN 'BOTH'
        ELSE 'ENROLLED'
        END AS read_scope,

    CASE
        WHEN b.create_role_list NOT LIKE '%COMMON%'
            THEN 'ONLY_ADMIN'
        ELSE 'ALL_USER'
        END AS write_scope,

    b.is_default_notice,

    CASE
        WHEN b.is_deleted = true THEN 'HIDDEN'
        ELSE 'VISIBLE'
        END AS visibility,

    ROW_NUMBER() OVER (ORDER BY b.created_at) * 10 AS display_order,

    NOW(6),
    NOW(6)
FROM tb_board b
WHERE NOT EXISTS (
    SELECT 1 FROM tb_board_config
);