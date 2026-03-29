-- Migration: AddPostColumnToSchedule

ALTER TABLE tb_schedule
    ADD COLUMN target_post_id VARCHAR(255) NULL;

