-- Migration: AddIsExtendedToLocker
ALTER TABLE tb_locker
    ADD COLUMN is_extended BIT(1) NOT NULL DEFAULT b'0';

ALTER TABLE tb_notification
    MODIFY COLUMN notice_type ENUM(
        'POST',
        'COMMENT',
        'CEREMONY',
        'BOARD',
        'ADMISSION',
        'COMMUNITY',
        'SYSTEM',
        'OFFICIAL',
        'CEREMONY_V2',
        'LOCKER'
    ) NULL;
