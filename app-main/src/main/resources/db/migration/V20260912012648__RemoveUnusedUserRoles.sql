INSERT INTO user_roles (user_id, role)
SELECT user_id, 'COMMON'
FROM user_roles
GROUP BY user_id
HAVING SUM(role IN ('NONE', 'COMMON', 'ADMIN', 'SYSTEM_ADMIN')) = 0;

DELETE FROM user_roles
WHERE role NOT IN ('NONE', 'COMMON', 'ADMIN', 'SYSTEM_ADMIN');

ALTER TABLE user_roles
    MODIFY COLUMN role ENUM('NONE', 'COMMON', 'ADMIN', 'SYSTEM_ADMIN') NOT NULL;
