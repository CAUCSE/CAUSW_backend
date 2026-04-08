-- Migration: modifyNoticeTypeConstraint
alter table tb_notification
    modify notice_type enum ('POST', 'COMMENT', 'BOARD', 'ADMISSION', 'CEREMONY', 'COMMUNITY', 'SYSTEM', 'OFFICIAL', 'CEREMONY_V2') null;