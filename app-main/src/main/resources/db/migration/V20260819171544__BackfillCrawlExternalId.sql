-- Migration: BackfillCrawlExternalId
-- 중앙대 SW 공지의 외부 식별자를 링크의 code와 uid 조합으로 변경한다.
UPDATE tb_crawled_notice
SET external_id = CONCAT(
    SUBSTRING_INDEX(SUBSTRING_INDEX(link, 'code=', -1), '&', 1),
    ':',
    SUBSTRING_INDEX(SUBSTRING_INDEX(link, 'uid=', -1), '&', 1)
)
WHERE site_id = 'cau-sw-notice';
