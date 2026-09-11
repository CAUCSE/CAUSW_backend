-- Migration: AddAnonymousProfileImageToComment

ALTER TABLE tb_comment
    ADD COLUMN anonymous_profile_image_type VARCHAR(20) NULL COMMENT '익명 댓글에 부여된 랜덤 프로필 이미지 타입' AFTER anonymous_nickname;

ALTER TABLE tb_comment_anonymous_nickname
    ADD COLUMN profile_image_type VARCHAR(20) NULL COMMENT '게시글 내에서 부여된 랜덤 프로필 이미지 타입' AFTER nickname;
