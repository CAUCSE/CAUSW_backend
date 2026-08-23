-- 기존 크롤링 게시물의 본문은 제목·첨부파일·원문 링크를 포함한 레거시 HTML을 보존한다.
-- 따라서 별도 데이터 백필 없이 기본값 false로 추가하고, isCrawledContentSeparated 도입 시점부터 본문 분리 로직을 거친 게시물만 true로 기록한다.
ALTER TABLE tb_post
    ADD COLUMN is_crawled_content_separated TINYINT(1) NOT NULL DEFAULT 0;
