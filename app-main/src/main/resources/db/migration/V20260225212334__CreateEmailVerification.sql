-- Migration: CreateEmailVerification
CREATE TABLE `tb_email_verification`
(
    `id`                VARCHAR(255)                NOT NULL,
    `created_at`        DATETIME(6)                 DEFAULT NULL,
    `updated_at`        DATETIME(6)                 DEFAULT NULL,

    `email`             VARCHAR(255)                NOT NULL COMMENT '인증 대상 이메일',
    `status`            ENUM('PENDING', 'VERIFIED') NOT NULL COMMENT '인증 상태',
    `verification_code` VARCHAR(10)                 NOT NULL COMMENT '이메일 인증 번호',
    `expires_at`        DATETIME(6)                 NOT NULL COMMENT '인증 코드 만료 시각',

    PRIMARY KEY (`id`),

    INDEX `idx_email_verification_email` (`email`)
);
