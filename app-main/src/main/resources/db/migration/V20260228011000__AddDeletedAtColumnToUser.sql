-- User soft delete를 state 기반에서 deleted_at 기반으로 전환하기 위한 컬럼 추가
ALTER TABLE tb_user
    ADD COLUMN deleted_at DATETIME NULL;

-- deleted_at 기반 전환에 따라 state ENUM에서 DELETED 값 제거
ALTER TABLE tb_user
    MODIFY COLUMN `state` ENUM('AWAIT', 'ACTIVE', 'INACTIVE', 'REJECT', 'DROP', 'GUEST') NOT NULL;
