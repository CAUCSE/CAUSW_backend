-- Migration: ModifyAllStudentIdNullable
-- 사용자 테이블: 학번 NULL 허용
ALTER TABLE tb_user
    MODIFY COLUMN student_id VARCHAR(255) NULL;

-- 학적 로그 테이블: 대상 사용자/관리자 학번 NULL 허용
ALTER TABLE tb_user_academic_record_log
    MODIFY COLUMN target_user_student_id VARCHAR(255) NULL;

ALTER TABLE tb_user_academic_record_log
    MODIFY COLUMN controlled_user_student_id VARCHAR(255) NULL;

-- 학생회비 사용자 테이블: 학번 NULL 허용
ALTER TABLE tb_council_fee_fake_user
    MODIFY COLUMN student_id VARCHAR(255) NULL;

-- 학생회비 로그 테이블: 대상 사용자/관리자 학번 NULL 허용
ALTER TABLE tb_user_council_fee_log
    MODIFY COLUMN student_id VARCHAR(255) NULL;

ALTER TABLE tb_user_council_fee_log
    MODIFY COLUMN controlled_user_student_id VARCHAR(255) NULL;