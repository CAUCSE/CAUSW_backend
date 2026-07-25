UPDATE tb_post
SET is_deleted = b'0'
WHERE is_deleted IS NULL;

UPDATE tb_comment
SET is_deleted = b'0'
WHERE is_deleted IS NULL;

ALTER TABLE tb_post
    MODIFY COLUMN is_deleted BIT(1) NOT NULL DEFAULT b'0';

ALTER TABLE tb_comment
    MODIFY COLUMN is_deleted BIT(1) NOT NULL DEFAULT b'0';
