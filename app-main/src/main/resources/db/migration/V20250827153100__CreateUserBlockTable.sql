-- Migration: CreateUserBlockTable

CREATE TABLE tb_user_block (
                            id VARCHAR(36) NOT NULL PRIMARY KEY,
                            blocker_id VARCHAR(36) NOT NULL COMMENT '차단한 사용자 ID',
                            blockee_id VARCHAR(36) NOT NULL COMMENT '차단당한 사용자 ID',
                            scope VARCHAR(20) NOT NULL COMMENT '차단 경로 (POST, COMMENT, CHILD_COMMENT)',
                            scope_ref_id VARCHAR(36) NOT NULL COMMENT '스코프 대상 ID',
                            blockee_anonymous BOOLEAN NOT NULL DEFAULT FALSE COMMENT '피차단자가 당시 익명이었는지 여부',
                            content_snapshot LONGTEXT COMMENT '차단 트리거가 된 게시글/댓글 내용 일부 스냅샷',
                            active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성/비활성 플래그',
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    -- 유저-유저 쌍으로 유니크 인덱스 생성
                            CONSTRAINT uq_block_active UNIQUE (blocker_id, blockee_id),

    -- 기본 인덱스들
                            INDEX idx_blocker_active (blocker_id, active),
                            INDEX idx_blockee_active (blockee_id, active),
                            INDEX idx_scope_active (scope, scope_ref_id, active),

    -- 추가 성능 최적화 인덱스들
                            INDEX idx_blocker_blockee_active (blocker_id, blockee_id, active),
                            INDEX idx_created_at (created_at),
                            INDEX idx_updated_at (updated_at),
                            INDEX idx_scope_ref_id (scope_ref_id),
                            INDEX idx_active_created_at (active, created_at),

    -- 외래키 제약조건 (필요시 추가)
    CONSTRAINT fk_user_block_blocker FOREIGN KEY (blocker_id) REFERENCES tb_user (id),
    CONSTRAINT fk_user_block_blockee FOREIGN KEY (blockee_id) REFERENCES tb_user (id)
) COMMENT='사용자 차단 테이블';

-- 차단 스코프에 대한 체크 제약조건 (MySQL 8.0.16+)
ALTER TABLE tb_user_block
    ADD CONSTRAINT chk_block_scope
        CHECK (scope IN ('POST', 'COMMENT', 'CHILD_COMMENT'));

-- 자기 자신 차단 방지 체크 제약조건
ALTER TABLE tb_user_block
    ADD CONSTRAINT chk_no_self_block
        CHECK (blocker_id != blockee_id);