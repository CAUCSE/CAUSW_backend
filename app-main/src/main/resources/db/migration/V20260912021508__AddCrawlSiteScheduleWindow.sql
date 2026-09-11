-- Migration: AddCrawlSiteScheduleWindow
ALTER TABLE tb_crawl_site_config
    ADD COLUMN start_time TIME NULL AFTER is_enabled,
    ADD COLUMN end_time TIME NULL AFTER start_time;
