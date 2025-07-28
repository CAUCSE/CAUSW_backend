-- Migration: ModifyStudentIdNullable

ALTER TABLE tb_user
    MODIFY COLUMN student_id VARCHAR(255) NULL;

ALTER TABLE tb_user_academic_record_log
    MODIFY COLUMN target_user_student_id VARCHAR(255) NULL;
