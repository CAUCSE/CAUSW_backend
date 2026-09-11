-- Migration: AddSystemNoticeToNoticeType
-- 시스템 공지 알림(SYSTEM_NOTICE) 저장을 위해 notice_type ENUM 값을 확장한다.
-- 아울러 AdmissionNotificationListener/CeremonyAdminNotificationListener 가 이미 사용 중이던
-- ADMIN 값도 ENUM 목록에 누락되어 있었으므로 함께 추가한다.
-- 기존 데이터는 유지되며, 컬럼 정의만 갱신된다.
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
        'LOCKER',
        'SYSTEM_NOTICE',
        'ADMIN'
    ) NULL;
