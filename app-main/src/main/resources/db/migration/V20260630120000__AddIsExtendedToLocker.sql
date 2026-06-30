-- Migration: AddIsExtendedToLocker
ALTER TABLE tb_locker
    ADD COLUMN is_extended BIT(1) NOT NULL DEFAULT b'0';
