-- 1. 새로운 컬럼 추가
ALTER TABLE tb_user_info
    ADD COLUMN social_links TEXT;

-- 2. 기존 데이터 마이그레이션
UPDATE tb_user_info
SET social_links = JSON_ARRAY(
        github_link,
        linkedin_link,
        instagram_link,
        notion_link,
        blog_link)
WHERE github_link IS NOT NULL
   OR linkedin_link IS NOT NULL
   OR instagram_link IS NOT NULL
   OR notion_link IS NOT NULL
   OR blog_link IS NOT NULL;

-- 3. NULL 값 제거
UPDATE tb_user_info
SET social_links = (
    SELECT JSON_ARRAYAGG(link)
    FROM (
             SELECT github_link AS link FROM tb_user_info AS t WHERE t.id = tb_user_info.id AND github_link IS NOT NULL AND github_link != ''
             UNION ALL
             SELECT linkedin_link FROM tb_user_info AS t WHERE t.id = tb_user_info.id AND linkedin_link IS NOT NULL AND linkedin_link != ''
             UNION ALL
             SELECT instagram_link FROM tb_user_info AS t WHERE t.id = tb_user_info.id AND instagram_link IS NOT NULL AND instagram_link != ''
             UNION ALL
             SELECT notion_link FROM tb_user_info AS t WHERE t.id = tb_user_info.id AND notion_link IS NOT NULL AND notion_link != ''
             UNION ALL
             SELECT blog_link FROM tb_user_info AS t WHERE t.id = tb_user_info.id AND blog_link IS NOT NULL AND blog_link != ''
         ) AS links
)
WHERE github_link IS NOT NULL
   OR linkedin_link IS NOT NULL
   OR instagram_link IS NOT NULL
   OR notion_link IS NOT NULL
   OR blog_link IS NOT NULL;

-- 4. 기존 컬럼 삭제
ALTER TABLE tb_user_info
DROP COLUMN github_link,
DROP COLUMN linkedin_link,
DROP COLUMN instagram_link,
DROP COLUMN notion_link,
DROP COLUMN blog_link;