-- Migration: DropTermAgreementColumnsFromUser
-- 임베디드 TermAgreements(tb_user 컬럼 3개) 제거. 약관 동의는 tb_user_terms_agreement로 이전됨.

ALTER TABLE tb_user
    DROP COLUMN service_agreed_at,
    DROP COLUMN privacy_agreed_at,
    DROP COLUMN third_party_agreed_at;
