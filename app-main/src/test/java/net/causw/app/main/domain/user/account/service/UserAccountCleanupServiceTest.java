package net.causw.app.main.domain.user.account.service;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.notification.notification.service.implementation.UserPushTokenWriter;
import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountReader;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountUnlinkManager;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;

@ExtendWith(MockitoExtension.class)
public class UserAccountCleanupServiceTest {

	@InjectMocks
	private UserAccountCleanupService userAccountCleanupService;

	@Mock
	private SocialAccountReader socialAccountReader;

	@Mock
	private SocialAccountUnlinkManager socialAccountUnlinkManager;

	@Mock
	private AuthTokenManager authTokenManager;

	@Mock
	private UserPushTokenWriter userPushTokenWriter;

	private final String userId = "test-uuid";
	private final String accessToken = "access-token";
	private final String refreshToken = "refresh-token";
	private final String platformHint = "ios";

	@Test
	@DisplayName("회원 탈퇴 정리 시 소셜 unlink, 토큰 무효화, FCM 토큰 정리를 수행한다")
	void cleanupForWithdrawal_Success() {
		// given
		User user = mock(User.class);
		SocialAccount socialAccount = mock(SocialAccount.class);

		when(user.getId()).thenReturn(userId);
		when(socialAccountReader.findAllByUserId(userId)).thenReturn(List.of(socialAccount));

		// when
		userAccountCleanupService.cleanupForWithdrawal(user, accessToken, refreshToken, platformHint);

		// then
		verify(socialAccountReader).findAllByUserId(userId);
		verify(socialAccountUnlinkManager).unlink(socialAccount, platformHint);
		verify(authTokenManager).invalidateTokens(accessToken, refreshToken);
		verify(userPushTokenWriter).clearFcmTokens(user);
	}

	@Test
	@DisplayName("회원 탈퇴 정리 시 소셜 계정이 없어도 토큰 무효화와 FCM 토큰 정리를 수행한다")
	void cleanupForWithdrawal_Success_WithoutSocialAccounts() {
		// given
		User user = mock(User.class);

		when(user.getId()).thenReturn(userId);
		when(socialAccountReader.findAllByUserId(userId)).thenReturn(List.of());

		// when
		userAccountCleanupService.cleanupForWithdrawal(user, accessToken, refreshToken, platformHint);

		// then
		verify(socialAccountReader).findAllByUserId(userId);
		verify(authTokenManager).invalidateTokens(accessToken, refreshToken);
		verify(userPushTokenWriter).clearFcmTokens(user);
	}

	@Test
	@DisplayName("소셜 unlink 실패 시에도 회원 탈퇴 정리 흐름은 계속 진행한다")
	void cleanupForWithdrawal_Continue_WhenSocialUnlinkFails() {
		// given
		User user = mock(User.class);
		SocialAccount socialAccount = mock(SocialAccount.class);

		when(user.getId()).thenReturn(userId);
		when(socialAccountReader.findAllByUserId(userId)).thenReturn(List.of(socialAccount));
		doThrow(new RuntimeException("unlink failed"))
			.when(socialAccountUnlinkManager)
			.unlink(socialAccount, platformHint);

		// when
		userAccountCleanupService.cleanupForWithdrawal(user, accessToken, refreshToken, platformHint);

		// then
		verify(socialAccountUnlinkManager).unlink(socialAccount, platformHint);
		verify(authTokenManager).invalidateTokens(accessToken, refreshToken);
		verify(userPushTokenWriter).clearFcmTokens(user);
	}

	@Test
	@DisplayName("회원 추방 정리 시 소셜 unlink와 FCM 토큰 정리를 수행하고 토큰 무효화는 수행하지 않는다")
	void cleanupForDrop_Success() {
		// given
		User user = mock(User.class);
		SocialAccount socialAccount = mock(SocialAccount.class);

		when(user.getId()).thenReturn(userId);
		when(socialAccountReader.findAllByUserId(userId)).thenReturn(List.of(socialAccount));

		// when
		userAccountCleanupService.cleanupForDrop(user);

		// then
		verify(socialAccountReader).findAllByUserId(userId);
		verify(socialAccountUnlinkManager).unlink(socialAccount, null);
		verify(userPushTokenWriter).clearFcmTokens(user);
		verify(authTokenManager, never()).invalidateTokens(accessToken, refreshToken);
	}

	@Test
	@DisplayName("소셜 unlink 실패 시에도 회원 추방 정리 흐름은 계속 진행한다")
	void cleanupForDrop_Continue_WhenSocialUnlinkFails() {
		// given
		User user = mock(User.class);
		SocialAccount socialAccount = mock(SocialAccount.class);

		when(user.getId()).thenReturn(userId);
		when(socialAccountReader.findAllByUserId(userId)).thenReturn(List.of(socialAccount));
		doThrow(new RuntimeException("unlink failed"))
			.when(socialAccountUnlinkManager)
			.unlink(socialAccount, null);

		// when
		userAccountCleanupService.cleanupForDrop(user);

		// then
		verify(socialAccountUnlinkManager).unlink(socialAccount, null);
		verify(userPushTokenWriter).clearFcmTokens(user);
		verify(authTokenManager, never()).invalidateTokens(accessToken, refreshToken);
	}
}
