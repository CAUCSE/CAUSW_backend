-- Migration: AddAnonymousProfileImageToPost

ALTER TABLE tb_post
    ADD COLUMN anonymous_profile_image_type VARCHAR(20) NULL COMMENT '익명 게시글에 부여된 랜덤 프로필 이미지 타입' AFTER anonymous_nickname;
