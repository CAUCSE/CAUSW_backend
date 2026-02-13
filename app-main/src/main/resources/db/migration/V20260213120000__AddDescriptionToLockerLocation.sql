-- Migration: AddDescriptionToLockerLocation

ALTER TABLE TB_LOCKER_LOCATION
ADD COLUMN description VARCHAR(255) NULL;

UPDATE TB_LOCKER_LOCATION SET description = '2층' WHERE name = 'SECOND';
UPDATE TB_LOCKER_LOCATION SET description = '3층' WHERE name = 'THIRD';
UPDATE TB_LOCKER_LOCATION SET description = '4층' WHERE name = 'FOURTH';
