package net.causw.app.main.domain.user.auth.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;
import net.causw.app.main.domain.user.auth.crypto.OauthRefreshTokenCipher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Google/Apple OAuth 리프레시 토큰을 암호화해 {@link SocialAccount}에 저장합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SocialAccountOauthRefreshStore {

	private final SocialAccountRepository socialAccountRepository;
	private final OauthRefreshTokenCipher oauthRefreshTokenCipher;

	@Transactional
	public void saveEncryptedRefreshToken(String userId, SocialType socialType, String plainRefreshToken) {
		if (!StringUtils.hasText(plainRefreshToken)) {
			return;
		}
		Optional<SocialAccount> accountOpt = socialAccountRepository.findByUser_IdAndSocialType(userId, socialType);
		if (accountOpt.isEmpty()) {
			log.error("SocialAccount missing when saving OAuth refresh token. userId={}, socialType={}", userId,
				socialType);
			return;
		}
		SocialAccount account = accountOpt.get();
		account.replaceEncryptedOauthRefreshToken(oauthRefreshTokenCipher.encrypt(plainRefreshToken));
	}
}
