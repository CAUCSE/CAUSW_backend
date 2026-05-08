-- 탈퇴 상태를 UserState.INACTIVE 기준으로 다시 사용하기 위해 enum에 INACTIVE를 포함한다.
ALTER TABLE tb_user
    MODIFY COLUMN `state` ENUM('AWAIT', 'ACTIVE', 'INACTIVE', 'REJECT', 'DROP', 'GUEST') NOT NULL;

-- 기존 탈퇴 사용자(deleted_at 존재, ACTIVE 상태) 데이터를 INACTIVE로 이관한다.
UPDATE tb_user
SET state = 'INACTIVE'
WHERE state = 'ACTIVE'
  AND deleted_at IS NOT NULL;
