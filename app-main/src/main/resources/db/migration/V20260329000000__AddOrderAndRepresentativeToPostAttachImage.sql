ALTER TABLE tb_post_attach_image_uuid_file
    ADD COLUMN image_order INT NOT NULL DEFAULT 0,
    ADD COLUMN is_representative BIT(1) NOT NULL DEFAULT b'0';

