-- Migration: DropLatestCrawlTable

-- Drop tb_latest_crawl table (no longer needed with hash-based duplicate detection)
DROP TABLE IF EXISTS `tb_latest_crawl`;
