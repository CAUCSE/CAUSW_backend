-- Migration: AddIsHiddenToPost
ALTER TABLE tb_post
	ADD COLUMN is_hidden BOOLEAN NOT NULL DEFAULT FALSE;
