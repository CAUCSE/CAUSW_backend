-- 기존 환경의 content_snapshot 타입을 엔티티 및 초기 스키마 정의와 일치시킨다.
ALTER TABLE tb_user_block
    MODIFY COLUMN content_snapshot LONGTEXT NULL;
