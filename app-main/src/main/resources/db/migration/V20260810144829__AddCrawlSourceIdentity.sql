ALTER TABLE tb_crawled_notice
    ADD COLUMN site_id VARCHAR(100) NULL,
    ADD COLUMN external_id VARCHAR(255) NULL,
    ADD COLUMN target_board_id VARCHAR(255) NULL;

UPDATE tb_crawled_notice notice
JOIN tb_crawl_site_config config
  ON config.site_id = 'cau-sw-notice'
SET notice.site_id = config.site_id,
    notice.external_id = notice.link,
    notice.target_board_id = config.target_board_id;

ALTER TABLE tb_crawled_notice
    MODIFY COLUMN site_id VARCHAR(100) NOT NULL,
    MODIFY COLUMN external_id VARCHAR(255) NOT NULL,
    MODIFY COLUMN target_board_id VARCHAR(255) NOT NULL,
    DROP INDEX UK_5jsgsfwrca8bepsku2i3rjosl,
    ADD CONSTRAINT uk_crawled_notice_source UNIQUE (site_id, external_id),
    ADD INDEX idx_crawled_notice_link (link),
    ADD CONSTRAINT fk_crawled_notice_site_config
        FOREIGN KEY (site_id) REFERENCES tb_crawl_site_config (site_id),
    ADD CONSTRAINT fk_crawled_notice_target_board
        FOREIGN KEY (target_board_id) REFERENCES tb_board (id);
