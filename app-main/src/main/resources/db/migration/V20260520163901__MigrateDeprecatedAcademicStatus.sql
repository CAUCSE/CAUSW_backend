-- Migration: MigrateDeprecatedAcademicStatus
-- user 테이블에서 'ENROLLED'로 통합
UPDATE tb_user
SET academic_status = 'ENROLLED'
WHERE academic_status IN ('LEAVE_OF_ABSENCE', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR');

-- user_academic_record_log 테이블에서 행 삭제
DELETE FROM tb_user_academic_record_log
WHERE prior_academic_record_application_status IN ('LEAVE_OF_ABSENCE', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR');