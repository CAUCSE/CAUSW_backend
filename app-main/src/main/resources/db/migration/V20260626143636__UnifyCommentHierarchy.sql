-- Migration: UnifyCommentHierarchy

ALTER TABLE tb_comment
    ADD COLUMN parent_comment_id varchar(255) NULL;

INSERT INTO tb_comment (
    id,
    created_at,
    updated_at,
    content,
    is_deleted,
    post_id,
    user_id,
    is_anonymous,
    parent_comment_id
)
SELECT
    child.id,
    child.created_at,
    child.updated_at,
    child.content,
    child.is_deleted,
    parent.post_id,
    child.user_id,
    child.is_anonymous,
    child.parent_comment_id
FROM tb_child_comment child
JOIN tb_comment parent ON parent.id = child.parent_comment_id
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_comment existing
    WHERE existing.id = child.id
);

INSERT INTO tb_like_comment (
    id,
    created_at,
    updated_at,
    comment_id,
    user_id
)
SELECT
    child_like.id,
    child_like.created_at,
    child_like.updated_at,
    child_like.child_comment_id,
    child_like.user_id
FROM tb_like_child_comment child_like
WHERE child_like.child_comment_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM tb_like_comment existing
    WHERE existing.id = child_like.id
);

ALTER TABLE tb_comment
    ADD CONSTRAINT fk_comment_parent_comment
        FOREIGN KEY (parent_comment_id) REFERENCES tb_comment (id);

CREATE INDEX idx_comment_post_parent_created_at
    ON tb_comment (post_id, parent_comment_id, created_at);

CREATE INDEX idx_comment_parent_created_at
    ON tb_comment (parent_comment_id, created_at);
