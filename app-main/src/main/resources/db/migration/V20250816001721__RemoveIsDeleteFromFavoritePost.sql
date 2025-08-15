-- Migration: RemoveIsDeleteFromFavoritePost

-- is_deleted가 true인 데이터 삭제 (이미 즐겨찾기 해제된 데이터)
-- 이 데이터들은 실제로 즐겨찾기가 해제된 상태이므로 물리적으로 삭제
DELETE FROM tb_favorite_post WHERE is_deleted = true;

-- is_deleted 컬럼 제거
-- 남은 데이터는 모두 is_deleted = false인 활성 즐겨찾기들
ALTER TABLE tb_favorite_post DROP COLUMN is_deleted;