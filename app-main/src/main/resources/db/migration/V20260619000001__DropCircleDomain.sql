ALTER TABLE tb_board
    DROP COLUMN circle_id;

ALTER TABLE tb_form
    DROP COLUMN circle_id;

DROP TABLE IF EXISTS tb_circle_member;
DROP TABLE IF EXISTS tb_circle_main_image_uuid_file;
DROP TABLE IF EXISTS tb_circle;
