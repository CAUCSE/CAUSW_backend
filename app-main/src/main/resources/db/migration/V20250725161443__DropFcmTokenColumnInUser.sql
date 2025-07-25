-- Migration: DropFcmTokenColumnInUser

ALTER TABLE tb_user
DROP COLUMN fcm_token;