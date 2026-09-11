-- Migration: CreateSuccessStoryBoard

-- 자유 게시판 이름 변경
UPDATE tb_board
SET name = '자유',
    updated_at = NOW()
WHERE name = '자유 게시판';


-- 합격수기 게시판 추가
SET @new_board_id = UUID();

INSERT INTO tb_board (
    id, name, description, create_role_list, category,
    is_deleted, is_default, is_alumni, is_home, is_anonymous_allowed, is_default_notice,
    created_at, updated_at
)
VALUES (
   @new_board_id, '합격수기', '합격수기 공유 게시판입니다.',
   'ADMIN,VICE_PRESIDENT,PRESIDENT,LEADER_CIRCLE,LEADER_1,LEADER_2,LEADER_3,LEADER_4,COUNCIL,COMMON,ADMIN,PRESIDENT', 'COMMUNITY',
   false, false, false, false, true, false,
   NOW(), NOW()
);

-- 합격수기 게시판 설정 추가 (display_order는 기존 게시판 설정의 최대값 + 10으로 설정 (맨 마지막 위치))
INSERT INTO tb_board_config (
    board_id, is_anonymous, read_scope, write_scope, is_notice,
    visibility, display_order, created_at, updated_at
)
SELECT
    @new_board_id, 1, 'BOTH', 'ALL_USER', 0,
    'VISIBLE', COALESCE(MAX(display_order), 0) + 10, NOW(), NOW()
FROM tb_board_config;