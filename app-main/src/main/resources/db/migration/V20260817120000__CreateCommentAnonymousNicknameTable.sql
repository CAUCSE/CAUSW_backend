-- Migration: CreateCommentAnonymousNicknameTable

CREATE TABLE tb_comment_anonymous_nickname (
                                                id VARCHAR(36) NOT NULL PRIMARY KEY,
                                                post_id VARCHAR(36) NOT NULL COMMENT '게시글 ID',
                                                user_id VARCHAR(36) NOT NULL COMMENT '익명 댓글 작성자 ID',
                                                nickname VARCHAR(30) NOT NULL COMMENT '게시글 내에서 부여된 랜덤 익명 닉네임',
                                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    -- 게시글 안에서 사용자는 닉네임 하나만 가짐 (동일 사용자 재사용 보장)
                                                CONSTRAINT uq_comment_anonymous_nickname_post_user UNIQUE (post_id, user_id),
    -- 게시글 안에서 닉네임 중복 방지 (동시성 최후 방어선)
                                                CONSTRAINT uq_comment_anonymous_nickname_post_nickname UNIQUE (post_id, nickname),

                                                INDEX idx_comment_anonymous_nickname_post (post_id),
                                                INDEX idx_comment_anonymous_nickname_user (user_id),

                                                CONSTRAINT fk_comment_anonymous_nickname_post FOREIGN KEY (post_id) REFERENCES tb_post (id),
                                                CONSTRAINT fk_comment_anonymous_nickname_user FOREIGN KEY (user_id) REFERENCES tb_user (id)
) COMMENT='게시글별 익명 댓글 랜덤 닉네임 매핑 테이블';

ALTER TABLE tb_comment
    ADD COLUMN anonymous_nickname VARCHAR(30) NULL COMMENT '익명 댓글에 부여된 랜덤 닉네임' AFTER is_anonymous;
