-- Migration: ModifyIsAlumniInBoardTypeToBit

ALTER TABLE tb_board
    MODIFY COLUMN is_alumni BIT DEFAULT b'0' NOT NULL;
