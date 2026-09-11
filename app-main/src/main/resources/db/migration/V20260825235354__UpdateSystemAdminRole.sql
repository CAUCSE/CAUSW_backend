-- Migration: UpdateSystemAdminRole
-- DB의 Role Enum에 'SYSTEM_ADMIN' 추가
ALTER TABLE user_roles
MODIFY COLUMN role ENUM(
    'ADMIN', 'PRESIDENT', 'VICE_PRESIDENT', 'COUNCIL',
    'LEADER_1', 'LEADER_2', 'LEADER_3', 'LEADER_4',
    'LEADER_CIRCLE', 'LEADER_ALUMNI', 'COMMON', 'NONE',
    'PROFESSOR', 'ALUMNI_MANAGER', 'SYSTEM_ADMIN'
) NOT NULL;

-- 권한 업데이트
UPDATE user_roles ur
JOIN tb_user u ON u.id = ur.user_id
SET ur.role = 'SYSTEM_ADMIN'
WHERE u.email IN (
    'SYSTEM_CRAWLER_ACCOUNT', -- 크롤링 봇
    'admindongne@cau.ac.kr', -- 운영계 최고 관리자
    'admin@cau.ac.kr' -- 개발계 최고 관리자
)