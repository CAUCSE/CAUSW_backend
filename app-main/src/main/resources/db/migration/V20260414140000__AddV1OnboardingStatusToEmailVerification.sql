-- Migration: AddV1OnboardingStatusToEmailVerification
ALTER TABLE `tb_email_verification`
    MODIFY COLUMN `status` ENUM('PENDING', 'VERIFIED', 'PASSWORD_FIND', 'V1_ONBOARDING_PENDING', 'V1_ONBOARDING_VERIFIED') NOT NULL COMMENT '인증 상태';
