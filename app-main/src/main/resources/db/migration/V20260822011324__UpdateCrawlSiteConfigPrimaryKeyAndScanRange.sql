ALTER TABLE tb_crawl_site_config
    ADD COLUMN max_scan_range_days INT NULL AFTER max_articles;

UPDATE tb_crawl_site_config
SET max_scan_range_days = 3
WHERE max_scan_range_days IS NULL;

ALTER TABLE tb_crawl_site_config
    MODIFY COLUMN max_scan_range_days INT NOT NULL,
    DROP PRIMARY KEY,
    DROP INDEX uk_crawl_site_config_site_id,
    DROP COLUMN id,
    ADD PRIMARY KEY (site_id);
