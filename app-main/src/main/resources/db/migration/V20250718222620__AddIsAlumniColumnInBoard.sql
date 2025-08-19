-- Migration: AddIsAlumniColumnInBoard

ALTER TABLE tb_board
ADD COLUMN is_alumni BOOLEAN NOT NULL DEFAULT FALSE;