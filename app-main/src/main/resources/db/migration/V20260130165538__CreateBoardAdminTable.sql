CREATE TABLE tb_board_admin (
                                id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
                                board_id VARCHAR(255) NOT NULL COMMENT '게시판 ID',
                                user_id VARCHAR(255) NOT NULL COMMENT '관리자 유저 ID',

                                created_at DATETIME(6) NOT NULL COMMENT '생성일시',
                                updated_at DATETIME(6) NOT NULL COMMENT '수정일시',

                                PRIMARY KEY (id),
                                KEY idx_board_admin_board_id (board_id),
                                KEY idx_board_admin_user_id (user_id),
                                UNIQUE KEY uk_board_admin_board_user (board_id, user_id)
) COMMENT='게시판 관리자 매핑 테이블';