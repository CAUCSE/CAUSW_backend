package net.causw.app.main.domain.user.account.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.auth.crypto.OauthRefreshTokenCipher;
import net.causw.app.main.domain.user.auth.service.implementation.AppleOAuthRevokeClient;
import net.causw.app.main.domain.user.auth.service.implementation.GoogleOAuthRevokeClient;
import net.causw.app.main.domain.user.auth.service.implementation.KakaoOAuthUnlinkClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialAccountUnlinkManager {
	private final OauthRefreshTokenCipher oauthRefreshTokenCipher;
	private final GoogleOAuthRevokeClient googleOAuthRevokeClient;
	private final AppleOAuthRevokeClient appleOAuthRevokeClient;
	private final KakaoOAuthUnlinkClient kakaoOAuthUnlinkClient;

	/**
	 * 개별 소셜 계정의 연동 해제(Revoke/Unlink)를 관리합니다.
	 * <p>
	 * 소셜 타입별로 최적화된 해제 방식을 사용하며,
	 * 처리 후 보안을 위해 DB 내 OAuth 리프레시 토큰 정보를 제거합니다.
	 * </p>
	 *
	 * @param socialAccount 연동을 해제할 소셜 계정 엔티티
	 */
	public void unlink(SocialAccount socialAccount) {
		SocialType socialType = socialAccount.getSocialType();

		try {
			// 카카오: 어드민 키로 바로 해제
			if (socialType == SocialType.KAKAO) {
				revokeKakao(socialAccount);
			}
			// 구글/애플: 리프레시 토큰 기반 해제
			else {
				String encryptedRefreshToken = socialAccount.getOauthRefreshTokenCipher();
				if (!StringUtils.hasText(encryptedRefreshToken)) {
					log.warn("[Social Unlink] OAuth 리프레시 토큰이 존재하지 않습니다. SocialAccount ID: {}, User Email: {}",
						socialAccount.getId(),
						socialAccount.getUser().getEmail());
					return;
				}
				String refreshToken = oauthRefreshTokenCipher.decrypt(encryptedRefreshToken);
				revokeByProvider(socialType, refreshToken);
			}
		} catch (Exception e) {
			log.warn("소셜 연동 해제 실패. socialAccountId={}, socialType={}",
				socialAccount.getId(), socialAccount.getSocialType(), e);
		} finally {
			// 연동 해제 시도 후에는 DB상의 토큰 정보도 제거 (엔티티 메서드 활용)
			socialAccount.replaceEncryptedOauthRefreshToken(null);
		}
	}

	private void revokeKakao(SocialAccount socialAccount) {
		try {
			kakaoOAuthUnlinkClient.unlinkWithAdminKey(socialAccount.getId());
		} catch (Exception e) {
			log.error("[Kakao Unlink] 어드민 키를 활용한 강제 해제 실패. SocialAccount ID: {}", socialAccount.getId(), e);
			throw e;
		}
	}

	private void revokeByProvider(SocialType socialType, String refreshToken) {
		switch (socialType) {
			case GOOGLE -> googleOAuthRevokeClient.revoke(refreshToken);
			case APPLE -> appleOAuthRevokeClient.revoke(refreshToken);
			default -> log.info("해당 소셜 타입에 대한 핸들러가 없습니다: {}", socialType);
		}
	}
}
