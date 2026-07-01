-- Migration: AddPasswordFindStatusToEmailVerification
ALTER TABLE `tb_email_verification`
    MODIFY COLUMN `status` ENUM('PENDING', 'VERIFIED', 'PASSWORD_FIND') NOT NULL COMMENT '인증 상태';
