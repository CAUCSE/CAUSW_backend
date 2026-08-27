-- 소식 게시글 성격 분류 컬럼 추가 (null = 미분류)
ALTER TABLE tb_post ADD COLUMN category VARCHAR(30) NULL;
