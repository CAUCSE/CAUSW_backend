-- Migration: BackfillUserInfoDescriptionFromJob
-- v2부터 동문수첩에서 직업(job)은 소개글(description)과 역할이 겹쳐 description으로 통합됨.
-- job에 값이 있고 description이 비어있는 경우에만 description으로 job 값을 복사한다.
-- 기존 description이 존재하는 데이터는 덮어쓰지 않는다.

UPDATE tb_user_info
SET description = job
WHERE job IS NOT NULL
  AND TRIM(job) <> ''
  AND (description IS NULL OR TRIM(description) = '');
