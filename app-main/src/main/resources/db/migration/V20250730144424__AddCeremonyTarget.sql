-- Migration: AddCeremonyTarget

-- ceremony 테이블 삭제
DROP TABLE IF EXISTS ceremony;

-- 경조사 (tb_ceremony) 테이블에 새로운 필드 추가
ALTER TABLE tb_ceremony
ADD COLUMN is_set_all BIT(1) NOT NULL DEFAULT 0;

-- 경조사 대상 학번을 저장할 별도 테이블 생성
CREATE TABLE tb_ceremony_target_admission_years (
    ceremony_id VARCHAR(255) NOT NULL,
    admission_year VARCHAR(255) NOT NULL,
    INDEX idx_ceremony_id (ceremony_id),
    CONSTRAINT fk_ceremony_target_admission_years_ceremony
        FOREIGN KEY (ceremony_id) REFERENCES tb_ceremony(id) ON DELETE CASCADE
);

-- 기존 CeremonyNotificationSetting의 is_set_all 기본값 true로 변경
ALTER TABLE TB_CEREMONY_PUSH_NOTIFICATION
MODIFY COLUMN is_set_all BOOLEAN NOT NULL DEFAULT TRUE;