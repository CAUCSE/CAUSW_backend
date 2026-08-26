-- 정책 변경 전에 좋아요 1~4개를 이미 달성한 기존 게시글은 해당 마일스톤을 소비한 것으로 처리한다.
-- 이후 좋아요 취소와 재등록으로 같은 마일스톤에 재도달해도 알림 및 알림 로그를 생성하지 않는다.
INSERT INTO tb_post_like_milestone_achievement (
    id,
    created_at,
    updated_at,
    post_id,
    trigger_user_id,
    milestone_count,
    status,
    suppression_reason,
    notification_id
)
WITH
post_like_counts AS (
    SELECT post_id, COUNT(*) AS like_count
    FROM tb_like_post
    WHERE post_id IS NOT NULL
    GROUP BY post_id
),
early_milestones AS (
    SELECT 1 AS milestone_count
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
)
SELECT
    UUID(),
    NOW(6),
    NOW(6),
    post_like_counts.post_id,
    NULL,
    early_milestones.milestone_count,
    'BASELINED',
    NULL,
    NULL
FROM post_like_counts
INNER JOIN early_milestones
    ON early_milestones.milestone_count <= post_like_counts.like_count;
