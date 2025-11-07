-- Migration: AddCrawledNoticeUpdateFields

-- CrawledNotice 테이블에 업데이트 감지 필드 추가
ALTER TABLE tb_crawled_notice 
ADD COLUMN content_hash VARCHAR(64) NOT NULL DEFAULT '',
ADD COLUMN last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN is_updated BOOLEAN DEFAULT FALSE;

-- 인덱스 추가 (성능 최적화)
CREATE INDEX idx_crawled_notice_content_hash ON tb_crawled_notice(content_hash);
CREATE INDEX idx_crawled_notice_last_modified ON tb_crawled_notice(last_modified);
CREATE INDEX idx_crawled_notice_is_updated ON tb_crawled_notice(is_updated);

-- 기존 데이터의 content_hash 업데이트 (기본값 제거)
UPDATE tb_crawled_notice 
SET content_hash = SHA2(CONCAT(content, COALESCE(image_link, ''), COALESCE(link, '')), 256)
WHERE content_hash = '';

-- 기본값 제거 (NOT NULL 제약조건 유지)
ALTER TABLE tb_crawled_notice 
MODIFY COLUMN content_hash VARCHAR(64) NOT NULL;
