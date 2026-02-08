package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.infra.firebase.FcmUtils;
import net.causw.app.main.shared.infra.redis.RedisUtils;

@ExtendWith(MockitoExtension.class)
public class UserNotificationServiceTest {
	@InjectMocks
	private UserNotificationService userNotificationService;

	@Mock
	private UserReader userReader;
	@Mock
	private RedisUtils redisUtils;
	@Mock
	private FcmUtils fcmUtils;

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
			verify(fcmUtils, times(1)).cleanInvalidFcmTokens(mockUser);
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
			given(redisUtils.getRefreshTokenData(validRefreshToken)).willReturn(userId);
			given(userReader.findUserById(userId)).willReturn(mockUser);

			// when
			userNotificationService.createFcmToken(userId, fcmToken, validRefreshToken);

			// then
			verify(fcmUtils).cleanInvalidFcmTokens(mockUser);
			verify(fcmUtils).addFcmToken(mockUser, validRefreshToken, fcmToken);
		}

		@Test
		@DisplayName("실패: Redis에 리프레시 토큰이 없으면(만료/삭제) 예외가 발생한다")
		void fail_redis_null() {
			// given
			String expiredToken = "expired_token";
			given(redisUtils.getRefreshTokenData(expiredToken)).willReturn(null);

			// when & then
			assertThatThrownBy(() -> userNotificationService.createFcmToken(userId, fcmToken, expiredToken))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);

			// then
			verify(userReader, never()).findUserById(anyString());
		}

		@Test
		@DisplayName("실패: 리프레시 토큰의 주인과 요청한 유저가 다르면 예외가 발생한다")
		void fail_user_mismatch() {
			// given
			String otherUserId = "user-456";
			given(redisUtils.getRefreshTokenData(validRefreshToken)).willReturn(otherUserId);

			// when & then
			assertThatThrownBy(() -> userNotificationService.createFcmToken(userId, fcmToken, validRefreshToken))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);

			// then
			verify(userReader, never()).findUserById(anyString());
		}
	}
}
