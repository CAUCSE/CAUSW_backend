package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.notification.notification.service.implementation.UserPushTokenWriter;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserFcmTokenResponse;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

@ExtendWith(MockitoExtension.class)
public class UserNotificationServiceTest {
	@InjectMocks
	private UserNotificationService userNotificationService;

	@Mock
	private UserReader userReader;
	@Mock
	private UserPushTokenWriter userPushTokenWriter;

	@Nested
	@DisplayName("FCM 토큰 조회 (getFcmTokenByUser)")
	class GetFcmTokenTest {

		@Test
		@DisplayName("성공: 유저 조회 후 FCM 토큰 목록을 반환한다")
		void success() {
			// given
			String userId = "user-123";
			User mockUser = mock(User.class);
			given(userReader.findUserById(userId)).willReturn(mockUser);
			given(mockUser.getFcmTokens()).willReturn(Set.of("token-1"));

			// when
			UserFcmTokenResponse result = userNotificationService.findFcmTokenByUser(userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.fcmToken()).isEqualTo(Set.of("token-1"));
		}
	}

	@Nested
	@DisplayName("FCM 토큰 등록 (createFcmToken)")
	class RegisterFcmTokenTest {

		private User mockUser;
		private final String userId = "user-123";
		private final String fcmToken = "new_fcm_token";

		@BeforeEach
		void setUp() {
			mockUser = mock(User.class);
		}

		@Test
		@DisplayName("성공: 기기 단위로 FCM 토큰을 등록한다")
		void success_register() {
			// given
			given(userReader.findUserById(userId)).willReturn(mockUser);
			given(mockUser.getFcmTokens()).willReturn(Set.of(fcmToken));

			// when
			UserFcmTokenResponse result = userNotificationService.createFcmToken(userId, fcmToken);

			// then
			verify(userPushTokenWriter).addFcmToken(mockUser, fcmToken);
			assertThat(result.fcmToken()).isEqualTo(Set.of(fcmToken));
		}
	}
}
