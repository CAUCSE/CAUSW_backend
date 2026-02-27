-- Migration: ModifyUserAdmissionAndLogTable
-- 1) tb_user_admission: v2에서 확장된 컬럼명을 requested_* 접두사 붙도록 변경
-- 2) tb_user_admission_log: v2 확장 필드 추가

-- ── tb_user_admission 컬럼 리네이밍 ──

ALTER TABLE tb_user_admission RENAME COLUMN target_academic_status TO requested_academic_status;
ALTER TABLE tb_user_admission RENAME COLUMN student_id TO requested_student_id;
ALTER TABLE tb_user_admission RENAME COLUMN admission_year TO requested_admission_year;
ALTER TABLE tb_user_admission RENAME COLUMN department TO requested_department;

-- ── tb_user_admission_log v2 확장 필드 추가 (기존 v1 로그는 NULL) ──

ALTER TABLE tb_user_admission_log ADD COLUMN requested_academic_status ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NULL;
ALTER TABLE tb_user_admission_log ADD COLUMN requested_student_id VARCHAR(50) NULL;
ALTER TABLE tb_user_admission_log ADD COLUMN requested_admission_year INT NULL;
ALTER TABLE tb_user_admission_log ADD COLUMN requested_department ENUM('DEPT_OF_CS', 'DEPT_OF_CSE', 'SCHOOL_OF_CSE', 'SCHOOL_OF_SW', 'DEPT_OF_AI') NULL;
