-- Migration: AddDepartmentColumInUser

ALTER TABLE tb_user
ADD COLUMN department ENUM('CS_DEPT', 'DEPT_OF_CSE', 'SCHOOL_OF_CSE', 'SW_SCHOOL', 'DEPT_OF_AI') NULL; -- null 임시 허용