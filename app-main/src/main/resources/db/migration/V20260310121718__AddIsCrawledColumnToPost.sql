-- Migration: AddIsCrawledColumnToPost

ALTER TABLE tb_post
    ADD COLUMN is_crawled TINYINT(1) NOT NULL DEFAULT 0;

-- 기존 크롤링 게시글에 is_crawled = true 설정
-- 조건: tb_crawled_notice의 title과 tb_post의 title이 일치하고,
--       해당 게시글이 크롤링 전용 게시판("소프트웨어학부 학부 공지")에 속하며,
--       작성자가 관리자 계정(student_id = '20220881')인 경우
UPDATE tb_post p
    INNER JOIN tb_board b ON p.board_id = b.id
SET p.is_crawled = 1
WHERE b.name = '소프트웨어학부 학부 공지'
  AND p.is_deleted = 0
  AND EXISTS (
      SELECT 1 FROM tb_crawled_notice cn WHERE cn.title = p.title
  );

