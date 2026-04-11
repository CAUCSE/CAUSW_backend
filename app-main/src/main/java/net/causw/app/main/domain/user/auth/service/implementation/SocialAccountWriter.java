package net.causw.app.main.domain.user.auth.service.implementation;

import java.util.List;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialAccountWriter {

	private final SocialAccountRepository socialAccountRepository;
	private final OAuthRefreshTokenCipher oAuthRefreshTokenCipher;
	private final GoogleOAuthRevokeClient googleOAuthRevokeClient;
	private final AppleOAuthRevokeClient appleOAuthRevokeClient;
	private final KakaoOAuthUnlinkClient kakaoOAuthUnlinkClient;

	// 사용자의 연동된 소셜 계정 모두 unlink/revoke 처리
	@Transactional
	public void unlinkAllByUser(User user) {
		List<SocialAccount> socialAccountList = socialAccountRepository.findAllByUserId(user.getId());

		for (SocialAccount socialAccount : socialAccountList) {
			unlink(socialAccount);
		}
	}

	public void unlink(SocialAccount socialAccount) {
		String encryptedRefreshToken = socialAccount.getOauthRefreshTokenCipher();

		if (!StringUtils.hasText(encryptedRefreshToken)) {
			return;
		}

		String refreshToken = oauthRefreshTokenCipher.decrypt(encryptedRefreshToken);

		try {
			revokeByProvider(socialAccount.getSocialType(), refreshToken);
		} catch (Exception e) {
			// 외부 provider revoke 실패 시에도 탈퇴 자체는 진행할 수 있도록 로그만 남김
			log.warn("Failed to revoke social provider token. socialAccountId={}, socialType={}",
				socialAccount.getId(), socialAccount.getSocialType(), e);
		} finally {
			socialAccount.replaceEncryptedOauthRefreshToken(null);
		}
	}

	private void revokeByProvider(SocialType socialType, String refreshToken) {
		if (!StringUtils.hasText(refreshToken)) {
			return;
		}

		switch (socialType) {
			case GOOGLE -> googleOAuthRevokeClient.revoke(refreshToken);
			case APPLE -> appleOAuthRevokeClient.revoke(refreshToken);
			case KAKAO -> kakaoOAuthUnlinkClient.unlink(refreshToken);
			default -> log.info("No revoke handler for socialType={}", socialType);
		}
	}
}
