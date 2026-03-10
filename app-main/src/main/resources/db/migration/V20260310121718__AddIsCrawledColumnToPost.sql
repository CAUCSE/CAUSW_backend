-- Migration: AddIsCrawledColumnToPost

ALTER TABLE tb_post
    ADD COLUMN is_crawled TINYINT(1) NOT NULL DEFAULT 0;

