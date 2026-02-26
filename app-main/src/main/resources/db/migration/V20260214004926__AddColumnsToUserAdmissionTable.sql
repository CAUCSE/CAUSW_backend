-- Migration: AddColumnsToUserAdmissionTable
-- v2 확장 필드 추가 (기존 v1 데이터는 NULL 허용)

ALTER TABLE tb_user_admission
    ADD COLUMN target_academic_status ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NULL,
    ADD COLUMN student_id VARCHAR(50) NULL,
    ADD COLUMN admission_year INT NULL,
    ADD COLUMN department ENUM('DEPT_OF_CS', 'DEPT_OF_CSE', 'SCHOOL_OF_CSE', 'SCHOOL_OF_SW', 'DEPT_OF_AI') NULL;
