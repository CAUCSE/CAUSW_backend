-- Migration: AddIsCrawledColumnToPost

ALTER TABLE tb_post
    ADD COLUMN is_crawled TINYINT(1) NOT NULL DEFAULT 0;

-- 기존 크롤링 게시글에 is_crawled = true 설정
UPDATE tb_post p
    INNER JOIN tb_board b ON p.board_id = b.id
SET p.is_crawled = 1
WHERE b.name = '소프트웨어학부 학부 공지';

