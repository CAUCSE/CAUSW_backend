ALTER TABLE tb_crawled_notice
    ADD COLUMN post_id VARCHAR(255) NULL;

UPDATE tb_crawled_notice notice
JOIN (
    SELECT MIN(crawled.id) AS notice_id,
           MIN(post.id) AS post_id
    FROM tb_crawled_notice crawled
    JOIN tb_post post
      ON post.title = crawled.title
     AND post.is_crawled = 1
     AND post.is_deleted = 0
    GROUP BY crawled.title
    HAVING COUNT(DISTINCT crawled.id) = 1
       AND COUNT(DISTINCT post.id) = 1
) matched ON matched.notice_id = notice.id
SET notice.post_id = matched.post_id;

ALTER TABLE tb_crawled_notice
    ADD CONSTRAINT uk_crawled_notice_post UNIQUE (post_id),
    ADD CONSTRAINT fk_crawled_notice_post
        FOREIGN KEY (post_id) REFERENCES tb_post (id);
