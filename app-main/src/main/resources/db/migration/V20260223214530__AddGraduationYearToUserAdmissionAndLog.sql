-- Migration: AddGraduationYearToUserAdmissionAndLog
--   v2에서 재학 심사 시 학적 정보를 함께 수집하도록 변경되었습니다.
--   이때 학적 상태를 '졸업(GRADUATED)'으로 선택한 경우 졸업 연도를 함께 받습니다.
ALTER TABLE tb_user_admission ADD COLUMN requested_graduation_year INT NULL;

ALTER TABLE tb_user_admission_log ADD COLUMN requested_graduation_year INT NULL;
