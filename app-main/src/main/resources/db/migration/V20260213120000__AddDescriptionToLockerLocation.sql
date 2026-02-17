-- Migration: AddDescriptionToLockerLocation

ALTER TABLE tb_local_location
ADD COLUMN description VARCHAR(255) NULL;

UPDATE tb_local_location SET description = '2층' WHERE name = 'SECOND';
UPDATE tb_local_location SET description = '3층' WHERE name = 'THIRD';
UPDATE tb_local_location SET description = '4층' WHERE name = 'FOURTH';
