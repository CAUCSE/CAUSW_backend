-- INACTIVE 상태 제거 전, 기존 INACTIVE 사용자를 deleted_at 기반 탈퇴로 이관한다.
-- 탈퇴 판정은 deleted_at 기준이므로 state는 ACTIVE로 정규화한다.
UPDATE tb_user
SET deleted_at = COALESCE(deleted_at, NOW()),
    state = 'ACTIVE'
WHERE state = 'INACTIVE';

-- user.state ENUM에서 INACTIVE 값을 제거한다.
ALTER TABLE tb_user
    MODIFY COLUMN `state` ENUM('AWAIT', 'ACTIVE', 'REJECT', 'DROP', 'GUEST') NOT NULL;
