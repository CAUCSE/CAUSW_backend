-- Migration: modifyNoticeTypeConstraint (안전한 버전)
alter table tb_notification
    modify notice_type enum (
    'POST',
    'COMMENT',
    'CEREMONY',
    'BOARD',
    'ADMISSION',
    'COMMUNITY',
    'SYSTEM',
    'OFFICIAL',
    'CEREMONY_V2'
    ) null;