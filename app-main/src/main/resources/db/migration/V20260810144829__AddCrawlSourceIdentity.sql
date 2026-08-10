ALTER TABLE tb_crawled_notice
    ADD COLUMN site_id VARCHAR(100) NULL,
    ADD COLUMN external_id VARCHAR(255) NULL;

UPDATE tb_crawled_notice
SET site_id = 'cau-sw-notice',
    external_id = link
WHERE site_id IS NULL
   OR external_id IS NULL;

ALTER TABLE tb_crawled_notice
    MODIFY COLUMN site_id VARCHAR(100) NOT NULL,
    MODIFY COLUMN external_id VARCHAR(255) NOT NULL,
    DROP INDEX UK_5jsgsfwrca8bepsku2i3rjosl,
    ADD CONSTRAINT uk_crawled_notice_source UNIQUE (site_id, external_id),
    ADD INDEX idx_crawled_notice_link (link),
    ADD CONSTRAINT fk_crawled_notice_site_config
        FOREIGN KEY (site_id) REFERENCES tb_crawl_site_config (site_id);
