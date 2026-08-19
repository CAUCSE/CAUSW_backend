-- Migration: BackfillCrawlExternalId
-- 동일 code와 uid를 가진 중복 공지는 가장 먼저 생성된 행만 남긴다.
DELETE file_link
FROM tb_crawled_file_link file_link
JOIN tb_crawled_notice target
    ON target.id = file_link.crawled_notice_id
JOIN tb_crawled_notice keeper
    ON target.site_id = 'cau-sw-notice'
   AND keeper.site_id = 'cau-sw-notice'
   AND CONCAT(
        SUBSTRING_INDEX(SUBSTRING_INDEX(target.link, 'code=', -1), '&', 1),
        ':',
        SUBSTRING_INDEX(SUBSTRING_INDEX(target.link, 'uid=', -1), '&', 1)
   ) = CONCAT(
        SUBSTRING_INDEX(SUBSTRING_INDEX(keeper.link, 'code=', -1), '&', 1),
        ':',
        SUBSTRING_INDEX(SUBSTRING_INDEX(keeper.link, 'uid=', -1), '&', 1)
   )
   AND (
        target.created_at > keeper.created_at
        OR (target.created_at = keeper.created_at AND target.id > keeper.id)
   );

DELETE FROM tb_crawled_notice
WHERE id IN (
    SELECT id
    FROM (
        SELECT target.id
        FROM tb_crawled_notice target
        JOIN tb_crawled_notice keeper
            ON target.site_id = 'cau-sw-notice'
           AND keeper.site_id = 'cau-sw-notice'
           AND CONCAT(
                SUBSTRING_INDEX(SUBSTRING_INDEX(target.link, 'code=', -1), '&', 1),
                ':',
                SUBSTRING_INDEX(SUBSTRING_INDEX(target.link, 'uid=', -1), '&', 1)
           ) = CONCAT(
                SUBSTRING_INDEX(SUBSTRING_INDEX(keeper.link, 'code=', -1), '&', 1),
                ':',
                SUBSTRING_INDEX(SUBSTRING_INDEX(keeper.link, 'uid=', -1), '&', 1)
           )
           AND (
                target.created_at > keeper.created_at
                OR (target.created_at = keeper.created_at AND target.id > keeper.id)
           )
    ) AS delete_targets
);

-- 중앙대 SW 공지의 외부 식별자를 링크의 code와 uid 조합으로 변경한다.
UPDATE tb_crawled_notice
SET external_id = CONCAT(
    SUBSTRING_INDEX(SUBSTRING_INDEX(link, 'code=', -1), '&', 1),
    ':',
    SUBSTRING_INDEX(SUBSTRING_INDEX(link, 'uid=', -1), '&', 1)
)
WHERE site_id = 'cau-sw-notice';
