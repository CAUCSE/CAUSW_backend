package net.causw.app.main.domain.user.account.service;

import java.util.List;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.notification.notification.service.implementation.UserPushTokenWriter;
import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountReader;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountUnlinkManager;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountCleanupService {

	private final SocialAccountReader socialAccountReader;
	private final SocialAccountUnlinkManager socialAccountUnlinkManager;
	private final AuthTokenManager authTokenManager;
	private final UserPushTokenWriter userPushTokenWriter;

	public void cleanupForWithdrawal(
		User user,
		String accessToken,
		String refreshToken,
		String platformHint) {
		unlinkSocialAccounts(user, platformHint);
		authTokenManager.invalidateTokens(accessToken, refreshToken);
		userPushTokenWriter.clearFcmTokens(user);
	}

	public void cleanupForDrop(User user) {
		unlinkSocialAccounts(user, null);
		userPushTokenWriter.clearFcmTokens(user);
	}

	private void unlinkSocialAccounts(User user, String platformHint) {
		List<SocialAccount> socialAccounts = socialAccountReader.findAllByUserId(user.getId());

		socialAccounts.forEach(socialAccount -> {
			try {
				socialAccountUnlinkManager.unlink(socialAccount, platformHint);
			} catch (RuntimeException e) {
				log.error("[User Account Cleanup] 소셜 연동 해제 실패. SocialType: {}, UserID: {}, Error: {}",
					socialAccount.getSocialType(),
					user.getId(),
					e.getMessage());
			}
		});
	}
}
