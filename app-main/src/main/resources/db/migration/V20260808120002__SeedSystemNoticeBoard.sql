INSERT INTO tb_board (
    id,
    created_at,
    updated_at,
    category,
    create_role_list,
    description,
    is_deleted,
    name,
    is_default,
    is_default_notice,
    is_anonymous_allowed,
    is_alumni,
    is_home
)
SELECT
    '00000000-0000-0000-0000-000000000001',
    NOW(6),
    NOW(6),
    'COMMUNITY',
    'ADMIN',
    '시스템 공지',
    b'0',
    '시스템 공지',
    b'0',
    b'0',
    b'0',
    b'0',
    b'0'
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_board
    WHERE id = '00000000-0000-0000-0000-000000000001'
);

INSERT INTO tb_board_config (
    board_id,
    is_anonymous,
    read_scope,
    write_scope,
    is_notice,
    is_system_notice,
    visibility,
    display_order,
    created_at,
    updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000001',
    FALSE,
    'BOTH',
    'ONLY_ADMIN',
    FALSE,
    TRUE,
    'VISIBLE',
    9999,
    NOW(6),
    NOW(6)
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_board_config
    WHERE board_id = '00000000-0000-0000-0000-000000000001'
);
