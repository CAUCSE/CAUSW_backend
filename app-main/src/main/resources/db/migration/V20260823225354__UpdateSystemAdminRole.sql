-- Migration: UpdateSystemAdminRole
UPDATE tb_user
SET role = 'SYSTEM_ADMIN'
WHERE email IN (
    'SYSTEM_CRAWLER_ACCOUNT', -- 크롤링 봇
    'admindongne@cau.ac.kr', -- 운영계 최고 관리자
    'admin@cau.ac.kr' -- 개발계 최고 관리자
)