-- Migration: ModifyMajorColumnInUserAndCouncilFeeLogNullable

ALTER TABLE tb_user
MODIFY COLUMN major VARCHAR(255) NULL;

ALTER TABLE tb_user_council_fee_log
MODIFY COLUMN major VARCHAR(255) NULL;