package net.causw.app.main.domain.user.account.service.implementation;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.notification.notification.service.implementation.UserPushTokenWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;

@ExtendWith(MockitoExtension.class)
class UserAccountCleanupWriterTest {

	@InjectMocks
	private UserAccountCleanupWriter userAccountCleanupWriter;

	@Mock
	private SocialAccountReader socialAccountReader;

	@Mock
	private SocialAccountUnlinkManager socialAccountUnlinkManager;

	@Mock
	private AuthTokenManager authTokenManager;

	@Mock
	private UserPushTokenWriter userPushTokenWriter;

	@Nested
	@DisplayName("회원 탈퇴 정리")
	class CleanupForWithdrawal {

		@Test
		@DisplayName("성공: 토큰 무효화와 FCM 토큰 전체 삭제를 수행한다")
		void givenUser_whenCleanupForWithdrawal_thenTokensInvalidatedAndCleared() {
			// given
			User user = mock(User.class);
			String userId = "user-id";
			String accessToken = "access-token";
			String refreshToken = "refresh-token";
			String platformHint = "ios";

			when(user.getId()).thenReturn(userId);
			when(socialAccountReader.findAllByUserId(userId)).thenReturn(List.of());

			// when
			userAccountCleanupWriter.cleanupForWithdrawal(user, accessToken, refreshToken, platformHint);

			// then
			verify(socialAccountReader).findAllByUserId(userId);
			verify(authTokenManager).invalidateTokens(accessToken, refreshToken);
			verify(userPushTokenWriter).clearFcmTokens(user);
		}
	}

	@Nested
	@DisplayName("관리자 추방 정리")
	class CleanupForDrop {

		@Test
		@DisplayName("성공: FCM 토큰 전체 삭제를 수행한다")
		void givenUser_whenCleanupForDrop_thenTokensCleared() {
			// given
			User user = mock(User.class);
			String userId = "user-id";

			when(user.getId()).thenReturn(userId);
			when(socialAccountReader.findAllByUserId(userId)).thenReturn(List.of());

			// when
			userAccountCleanupWriter.cleanupForDrop(user);

			// then
			verify(socialAccountReader).findAllByUserId(userId);
			verify(userPushTokenWriter).clearFcmTokens(user);
			verify(authTokenManager, never()).invalidateTokens(any(), any());
		}
	}
}
