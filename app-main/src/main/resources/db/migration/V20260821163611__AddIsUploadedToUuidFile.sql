-- Presigned URL 업로드 확정 여부 추적 컬럼 추가
-- 기존 레코드는 모두 multipart 업로드로 생성된 파일이므로 전체 TRUE로 백필
ALTER TABLE tb_uuid_file ADD COLUMN is_uploaded bit(1) NOT NULL DEFAULT b'0';
UPDATE tb_uuid_file SET is_uploaded = b'1';