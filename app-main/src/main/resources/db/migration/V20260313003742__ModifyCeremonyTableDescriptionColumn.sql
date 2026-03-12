-- Migration: ModifyCeremonyTableDescriptionColumn

ALTER TABLE tb_ceremony MODIFY COLUMN description TEXT;