-- UserAdmission description, UserAcademicRecordApplication note 컬럼 길이를 500자로 확장
ALTER TABLE tb_user_admission
    MODIFY COLUMN description VARCHAR(500) NULL;

ALTER TABLE tb_user_academic_record_application
    MODIFY COLUMN note VARCHAR(500) NULL;
