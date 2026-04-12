-- OAuth provider refresh token (Google/Apple unlink 등) AES-GCM 암호문 저장
ALTER TABLE tb_user_social_account
    ADD COLUMN oauth_refresh_token_cipher TEXT NULL COMMENT '암호화된 OAuth 리프레시 토큰 (Google/Apple)';
