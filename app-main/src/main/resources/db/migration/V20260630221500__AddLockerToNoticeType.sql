-- Migration: AddLockerToNoticeType
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
