-- Migration: AddOfficialProfileToBoardConfig

ALTER TABLE tb_board_config
ADD COLUMN official_nickname VARCHAR(255) DEFAULT NULL,
ADD COLUMN official_profile_image_url VARCHAR(255) DEFAULT NULL;