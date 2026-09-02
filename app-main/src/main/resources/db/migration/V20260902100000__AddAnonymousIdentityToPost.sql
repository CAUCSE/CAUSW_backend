-- Migration: AddAnonymousIdentityToPost

ALTER TABLE tb_post
    ADD COLUMN anonymous_nickname VARCHAR(30) NULL COMMENT '익명 게시글에 부여된 랜덤 닉네임' AFTER is_anonymous;
