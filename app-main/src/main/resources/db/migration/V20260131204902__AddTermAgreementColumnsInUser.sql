-- Migration: AddTermAgreementColumnsInUser

ALTER TABLE tb_user
    ADD COLUMN service_agreed_at DATETIME NULL COMMENT '서비스 이용약관 동의 일시',
    ADD COLUMN privacy_agreed_at DATETIME NULL COMMENT '개인정보 수집 및 이용 동의 일시',
    ADD COLUMN third_party_agreed_at DATETIME NULL COMMENT '제3자 정보제공 동의 일시';