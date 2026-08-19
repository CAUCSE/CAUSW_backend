CREATE TABLE tb_post_like_milestone_achievement
(
    id                 VARCHAR(255) NOT NULL,
    created_at         DATETIME(6)  NOT NULL,
    updated_at         DATETIME(6)  NOT NULL,
    post_id            VARCHAR(255) NOT NULL,
    trigger_user_id    VARCHAR(255) NULL,
    milestone_count    BIGINT       NOT NULL,
    status             VARCHAR(32)  NOT NULL,
    suppression_reason VARCHAR(32)  NULL,
    notification_id    VARCHAR(255) NULL,
    CONSTRAINT pk_post_like_milestone_achievement PRIMARY KEY (id),
    CONSTRAINT uk_post_like_milestone_achievement_post_milestone UNIQUE (post_id, milestone_count),
    CONSTRAINT uk_post_like_milestone_achievement_notification UNIQUE (notification_id),
    INDEX idx_post_like_milestone_achievement_trigger_user (trigger_user_id),
    CONSTRAINT fk_post_like_milestone_achievement_post
        FOREIGN KEY (post_id) REFERENCES tb_post (id),
    CONSTRAINT fk_post_like_milestone_achievement_trigger_user
        FOREIGN KEY (trigger_user_id) REFERENCES tb_user (id),
    CONSTRAINT fk_post_like_milestone_achievement_notification
        FOREIGN KEY (notification_id) REFERENCES tb_notification (id),
    CONSTRAINT chk_post_like_milestone_achievement_count CHECK (milestone_count > 0)
);

-- 기존 게시글은 현재 좋아요 수 이하의 마일스톤을 이미 소비한 것으로 처리한다.
-- 알림 및 알림 로그는 생성하지 않는다.
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
WITH RECURSIVE
post_like_counts AS (
    SELECT post_id, COUNT(*) AS like_count
    FROM tb_like_post
    WHERE post_id IS NOT NULL
    GROUP BY post_id
),
fixed_milestones AS (
    SELECT 5 AS milestone_count
    UNION ALL SELECT 10
    UNION ALL SELECT 50
    UNION ALL SELECT 100
    UNION ALL SELECT 500
),
thousand_milestones (post_id, like_count, milestone_count) AS (
    SELECT post_id, like_count, 1000
    FROM post_like_counts
    WHERE like_count >= 1000

    UNION ALL

    SELECT post_id, like_count, milestone_count + 1000
    FROM thousand_milestones
    WHERE milestone_count + 1000 <= like_count
),
reached_milestones AS (
    SELECT post_like_counts.post_id, fixed_milestones.milestone_count
    FROM post_like_counts
    INNER JOIN fixed_milestones
        ON fixed_milestones.milestone_count <= post_like_counts.like_count

    UNION ALL

    SELECT post_id, milestone_count
    FROM thousand_milestones
)
SELECT
    UUID(),
    NOW(6),
    NOW(6),
    post_id,
    NULL,
    milestone_count,
    'BASELINED',
    NULL,
    NULL
FROM reached_milestones;
