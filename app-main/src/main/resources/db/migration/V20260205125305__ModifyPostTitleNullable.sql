-- Migration: ModifyPostTitleNullable
ALTER TABLE tb_post
    MODIFY COLUMN title TEXT NULL;