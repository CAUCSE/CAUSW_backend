-- Migration: AddPostListIndex
-- 게시글 목록 조회 성능 개선을 위한 복합 인덱스 추가
-- created_at 기준 정렬 및 커서 기반 페이징 최적화

CREATE INDEX post_cursor_index ON tb_post (created_at, id);
