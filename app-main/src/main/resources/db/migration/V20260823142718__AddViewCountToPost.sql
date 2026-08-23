-- Migration: AddViewCountToPost
ALTER TABLE tb_post ADD COLUMN view_count BIGINT NOT NULL DEFAULT 0;