-- Migration: AddOfficialProfileToBoardConfig

ALTER TABLE tb_board_config
ADD COLUMN official_nickname VARCHAR(255) DEFAULT NULL,
ADD COLUMN official_profile_image_id VARCHAR(255) DEFAULT NULL;