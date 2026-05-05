-- Migration: CreateCrawledPostImageTable
-- 크롤링된 게시글 HTML에서 추출한 외부 이미지 URL을 저장하는 테이블

CREATE TABLE tb_crawled_post_image (
                                       id          VARCHAR(255)  NOT NULL PRIMARY KEY,
                                       post_id     VARCHAR(255)  NOT NULL,
                                       image_url   TEXT          NOT NULL,
                                       image_order INT           NOT NULL DEFAULT 0,
                                       created_at  DATETIME(6),
                                       updated_at  DATETIME(6),

                                       CONSTRAINT fk_crawled_post_image_post
                                           FOREIGN KEY (post_id) REFERENCES tb_post (id) ON DELETE CASCADE,

                                       INDEX idx_crawled_post_image_post_id (post_id)
);


