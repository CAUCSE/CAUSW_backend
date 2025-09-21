-- Migration: AddDepartmentColumInUser

ALTER TABLE tb_user
ADD COLUMN department ENUM('DEPT_OF_CS', 'DEPT_OF_CSE', 'SCHOOL_OF_CSE', 'SCHOOL_OF_SW', 'DEPT_OF_AI') NULL; -- null 임시 허용

ALTER TABLE tb_user_council_fee_log
ADD COLUMN department ENUM('DEPT_OF_CS', 'DEPT_OF_CSE', 'SCHOOL_OF_CSE', 'SCHOOL_OF_SW', 'DEPT_OF_AI') NULL; -- null 임시 허용