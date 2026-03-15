-- Migration: AddDisplayOrderIndexToBoardConfig
-- 게시판 목록 조회 시 display_order 기준 정렬 성능 개선

CREATE INDEX idx_board_config_display_order ON tb_board_config (display_order);
