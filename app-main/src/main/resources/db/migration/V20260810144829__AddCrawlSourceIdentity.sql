ALTER TABLE tb_crawled_notice
    ADD COLUMN site_id VARCHAR(100) NULL,
    ADD COLUMN external_id VARCHAR(255) NULL,
    ADD COLUMN target_board_id VARCHAR(255) NULL;

-- 기존 중앙대 SW 공지에 사이트 식별자를 부여하고 원본 링크를 외부 식별자로 백필한다.
UPDATE tb_crawled_notice
SET site_id = 'cau-sw-notice',
    external_id = link;

-- 기존 크롤링 Post가 가장 많이 저장된 활성 게시판을 기존 공지의 저장 대상 게시판으로 백필한다.
UPDATE tb_crawled_notice notice
JOIN (
    SELECT post.board_id
    FROM tb_post post
    WHERE post.is_crawled = 1
      AND post.is_deleted = 0
    GROUP BY post.board_id
    ORDER BY COUNT(*) DESC, post.board_id
    LIMIT 1
) target_board ON 1 = 1
SET notice.target_board_id = target_board.board_id;

ALTER TABLE tb_crawled_notice
    MODIFY COLUMN site_id VARCHAR(100) NOT NULL,
    MODIFY COLUMN external_id VARCHAR(255) NOT NULL,
    MODIFY COLUMN target_board_id VARCHAR(255) NOT NULL,
    DROP INDEX UK_5jsgsfwrca8bepsku2i3rjosl,
    ADD CONSTRAINT uk_crawled_notice_source UNIQUE (site_id, external_id),
    ADD INDEX idx_crawled_notice_link (link),
    ADD CONSTRAINT fk_crawled_notice_target_board
        FOREIGN KEY (target_board_id) REFERENCES tb_board (id);
