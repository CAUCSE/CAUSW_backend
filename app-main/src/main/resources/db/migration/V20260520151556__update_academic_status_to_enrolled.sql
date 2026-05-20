-- Migration: update_academic_status_to_enrolled

-- user 테이블에서 'ENROLLED'로 통합
UPDATE tb_user
SET academic_status = 'ENROLLED'
WHERE academic_status IN ('LEAVE_OF_ABSENCE', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR');