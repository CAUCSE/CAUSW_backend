-- Migration: AddIsHomeColumnInBoard

ALTER TABLE tb_board
ADD COLUMN is_home BIT DEFAULT b'0' NOT NULL;