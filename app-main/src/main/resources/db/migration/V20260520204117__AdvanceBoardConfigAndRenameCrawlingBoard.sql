-- Migration: Advance Board Config and Rename Crawling Board

-- 게시판 설정 테이블(tb_board_config)에 공식 닉네임과 공식 프로필 이미지 ID 컬럼을 추가합니다.
ALTER TABLE tb_board_config
ADD COLUMN official_nickname VARCHAR(255) DEFAULT NULL,
ADD COLUMN official_profile_image_id VARCHAR(255) DEFAULT NULL;

-- 기존 '소프트웨어학부 학부 공지'로 명명된 크롤링 게시판 이름을 '소프트웨어학부'로 일괄 변경합니다.
UPDATE tb_board
SET name = '소프트웨어학부'
WHERE name = '소프트웨어학부 학부 공지';