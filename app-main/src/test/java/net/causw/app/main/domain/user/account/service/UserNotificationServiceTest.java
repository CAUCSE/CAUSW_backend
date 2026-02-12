package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.user.account.api.v1.dto.UserFcmTokenResponseDto;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserPushTokenWriter;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

@ExtendWith(MockitoExtension.class)
public class UserNotificationServiceTest {
	@InjectMocks
	private UserNotificationService userNotificationService;

	@Mock
	private UserReader userReader;
	@Mock
	private UserPushTokenWriter userPushTokenWriter;
	@Mock
	private UserValidator userValidator;

	@Nested
	@DisplayName("FCM 토큰 조회 (getFcmTokenByUser)")
	class GetFcmTokenTest {

		@Test
		@DisplayName("성공: 유저 조회 및 만료된 토큰 정리가 수행되어야 한다")
		void success() {
			// given
			String userId = "user-123";
			User mockUser = mock(User.class);
			given(userReader.findUserById(userId)).willReturn(mockUser);

			// when
			UserFcmTokenResponseDto result = userNotificationService.findFcmTokenByUser(userId);

			// then
			verify(userPushTokenWriter, times(1)).cleanInvalidFcmTokens(mockUser);
			assertThat(result).isNotNull();
		}
	}

	@Nested
	@DisplayName("FCM 토큰 등록 (createFcmToken)")
	class RegisterFcmTokenTest {

		private User mockUser;
		private final String userId = "user-123";
		private final String validRefreshToken = "valid_refresh_token";
		private final String fcmToken = "new_fcm_token";

		@BeforeEach
		void setUp() {
			mockUser = mock(User.class);
		}

		@Test
		@DisplayName("성공: 리프레시 토큰이 유효하면 토큰을 정리하고 새로 등록한다")
		void success_register() {
			// given
			given(userReader.findUserById(userId)).willReturn(mockUser);

			// when
			userNotificationService.createFcmToken(userId, fcmToken, validRefreshToken);

			// then
			verify(userValidator).validateRefreshToken(userId, validRefreshToken);
			verify(userPushTokenWriter).cleanInvalidFcmTokens(mockUser);
			verify(userPushTokenWriter).addFcmToken(mockUser, validRefreshToken, fcmToken);
		}

		@Test
		@DisplayName("실패: Redis에 리프레시 토큰이 검증 실패 시(만료/삭제/불일치) 예외가 발생한다")
		void fail_redis_null() {
			// given
			String expiredToken = "expired_token";
			doThrow(AuthErrorCode.INVALID_REFRESH_TOKEN.toBaseException())
				.when(userValidator).validateRefreshToken(userId, expiredToken);

			// when & then
			assertThatThrownBy(() -> userNotificationService.createFcmToken(userId, fcmToken, expiredToken))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);

			// then
			verify(userReader, never()).findUserById(anyString());
			verify(userPushTokenWriter, never()).cleanInvalidFcmTokens(any());
			verify(userPushTokenWriter, never()).addFcmToken(any(), any(), any());
		}
	}
}
