-- Migration: MigrateDeprecatedAcademicStatus
-- user 테이블에서 'ENROLLED'로 통합
UPDATE tb_user
SET academic_status = 'ENROLLED'
WHERE academic_status IN ('LEAVE_OF_ABSENCE', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR');

-- user_academic_record_log 테이블에서 행 삭제
-- 이력 기록 테이블 특성상 과거의 기록을 수정하는 것이 적절치 않고,
-- DB에 'LEAVE_OF_ABSENCE'로 바꾸는 기록만 있어서 기록을 삭제하는 것이 적절하다고 판단함
DELETE FROM tb_user_academic_record_log
WHERE prior_academic_record_application_status IN ('LEAVE_OF_ABSENCE', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR');