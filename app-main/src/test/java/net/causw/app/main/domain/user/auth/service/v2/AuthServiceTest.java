package net.causw.app.main.domain.user.auth.service.v2;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.ConstraintViolationException;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.dto.request.UserRegisterDto;
import net.causw.app.main.domain.user.account.service.implementation.UserPushTokenWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.AuthService;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.AuthValidator;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationValidator;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@InjectMocks
	AuthService authService;

	@Mock
	private UserReader userReader;
	@Mock
	private UserWriter userWriter;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private UserValidator userValidator;
	@Mock
	private AuthValidator authValidator;
	@Mock
	private AuthTokenManager authTokenManager;
	@Mock
	private UserPushTokenWriter userPushTokenWriter;
	@Mock
	private EmailVerificationValidator emailVerificationValidator;

	private static final String USER_ID = "user_id_123";
	private static final String EMAIL = "test@example.com";
	private static final String PASSWORD = "password1234";
	private static final String ENCODED_PASSWORD = "encodedPassword";
	private static final String NAME = "TestUser";
	private static final String NICKNAME = "Nick";
	private static final String PHONE = "010-1234-5678";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String NEW_ACCESS_TOKEN = "new_access_token";
	private static final String NEW_REFRESH_TOKEN = "new_refresh_token";
	private static final String FCM_TOKEN = "fcm_token";

	private UserRegisterDto registerDto;
	private User user;
	private AuthTokenPair authTokenPair;

	@BeforeEach
	void setup() {
		registerDto = new UserRegisterDto(EMAIL, PASSWORD, NAME, NICKNAME, PHONE, "ABCD12");
		user = User.from(registerDto, ENCODED_PASSWORD);
		authTokenPair = new AuthTokenPair(ACCESS_TOKEN, REFRESH_TOKEN);
	}

	@Nested
	@DisplayName("이메일 회원가입 (registerEmailUser)")
	class RegisterEmailUserTest {

		@Test
		@DisplayName("성공: 모든 검증을 통과하면 사용자가 저장되고 응답을 반환한다.")
		void success() {
			// given
			given(userReader.checkUserExistByPhoneNumAndName(anyString(), anyString())).willReturn(Optional.empty());
			given(passwordEncoder.encode(anyString())).willReturn(ENCODED_PASSWORD);
			given(userWriter.save(any(User.class))).willReturn(user);

			// when
			AuthResult result = authService.registerEmailUser(registerDto);

			// then
			assertThat(result).isNotNull();
			assertThat(result.email()).isEqualTo(EMAIL);
			assertThat(result.accessToken()).isNull();
			assertThat(result.refreshToken()).isNull();

			// verify
			verify(userValidator).checkEmailDuplication(EMAIL);
			verify(userValidator).checkNicknameDuplication(NICKNAME);
			verify(userValidator).checkPhoneNumDuplication(PHONE);
			verify(authValidator).validateRegisterInput(any(User.class), eq(PASSWORD), eq(PHONE));
			verify(userWriter).save(any(User.class));
		}

		@Nested
		@DisplayName("회원가입 실패 케이스")
		class RegisterFailTest {

			@Nested
			@DisplayName("실패: 이미 가입된 사용자의 상태 문제")
			class UserAlreadyExist {

				@ParameterizedTest(name = "사용자 상태에 따른 에러코드: {0}")
				@EnumSource(value = UserErrorCode.class, names = {"ALREADY_REGISTERED", "USER_DROPPED",
					"USER_INACTIVE_CAN_REJOIN"})
				void fail_when_user_exists_with_invalid_state(UserErrorCode errorCode) {
					// given
					User mockedExistingUser = mock(User.class);
					given(userReader.checkUserExistByPhoneNumAndName(eq(PHONE), eq(NAME)))
						.willReturn(Optional.of(mockedExistingUser));

					doThrow(errorCode.toBaseException())
						.when(userValidator).validateUserStatusForSignup(any(User.class));

					// when & then
					assertThatThrownBy(() -> authService.registerEmailUser(registerDto))
						.isInstanceOf(BaseRunTimeV2Exception.class)
						.hasMessage(errorCode.getMessage());

					// verify
					verify(userValidator).validateUserStatusForSignup(any(User.class));
					verify(userValidator, never()).checkEmailDuplication(anyString());
				}
			}

			@Test
			@DisplayName("실패: 이메일이 중복되면 에러를 반환한다.")
			void fail_email_duplicate() {
				// given
				given(userReader.checkUserExistByPhoneNumAndName(anyString(), anyString()))
					.willReturn(Optional.empty());

				doThrow(UserErrorCode.EMAIL_ALREADY_EXIST.toBaseException())
					.when(userValidator).checkEmailDuplication(EMAIL);

				// when & then
				assertThatThrownBy(() -> authService.registerEmailUser(registerDto))
					.isInstanceOf(BaseRunTimeV2Exception.class);

				// verify
				verify(userValidator).checkEmailDuplication(EMAIL);
				verify(userValidator, never()).checkNicknameDuplication(anyString());
			}

			@Test
			@DisplayName("실패: 닉네임이 중복되면 에러를 반환한다.")
			void fail_nickname_duplicate() {
				// given
				given(userReader.checkUserExistByPhoneNumAndName(anyString(), anyString()))
					.willReturn(Optional.empty());

				doThrow(UserErrorCode.NICKNAME_ALREADY_EXIST.toBaseException())
					.when(userValidator).checkNicknameDuplication(NICKNAME);

				// when & then
				assertThatThrownBy(() -> authService.registerEmailUser(registerDto))
					.isInstanceOf(BaseRunTimeV2Exception.class);

				// verify
				verify(userValidator).checkNicknameDuplication(NICKNAME);
			}

			@Test
			@DisplayName("실패: 전화번호가 중복되면 에러를 반환한다.")
			void fail_phoneNumber_duplicate() {
				// given
				given(userReader.checkUserExistByPhoneNumAndName(anyString(), anyString()))
					.willReturn(Optional.empty());

				doThrow(UserErrorCode.PHONE_NUMBER_ALREADY_EXIST.toBaseException())
					.when(userValidator).checkPhoneNumDuplication(PHONE);

				// when & then
				assertThatThrownBy(() -> authService.registerEmailUser(registerDto))
					.isInstanceOf(BaseRunTimeV2Exception.class);

				// verify
				verify(userValidator).checkPhoneNumDuplication(PHONE);
			}

			@Nested
			@DisplayName("데이터 유효성 검증 실패 (AuthValidator Fail)")
			class ValidationFailTest {

				static Stream<Arguments> provideValidationExceptions() {
					return Stream.of(
						Arguments.of(new BadRequestException(ErrorCode.INVALID_USER_DATA_REQUEST, "비밀번호 형식이 잘못되었습니다.")),
						Arguments.of(new BadRequestException(ErrorCode.INVALID_USER_DATA_REQUEST, "전화번호 형식이 잘못되었습니다.")),
						Arguments.of(new ConstraintViolationException("Validation failed", Set.of())));
				}

				@ParameterizedTest(name = "발생 예외: {0}")
				@MethodSource("provideValidationExceptions")
				void fail_when_validation_throws_exception(Exception exceptionToThrow) {
					// given
					given(userReader.checkUserExistByPhoneNumAndName(anyString(), anyString()))
						.willReturn(Optional.empty());
					given(passwordEncoder.encode(anyString())).willReturn(ENCODED_PASSWORD);

					doThrow(exceptionToThrow)
						.when(authValidator).validateRegisterInput(any(User.class), eq(PASSWORD), eq(PHONE));

					// when & then
					assertThatThrownBy(() -> authService.registerEmailUser(registerDto))
						.isInstanceOf(exceptionToThrow.getClass());

					// verify
					verify(userWriter, never()).save(any(User.class));
				}
			}
		}
	}

	@Nested
	@DisplayName("이메일 로그인 (loginEmailUser)")
	class LoginEmailUserTest {

		@Test
		@DisplayName("성공: 로그인 검증 통과 시 토큰을 발급한다.")
		void success() {
			// given
			given(userReader.findByEmailOrElseThrow(EMAIL)).willReturn(user);
			given(authTokenManager.issueTokens(any(User.class), any())).willReturn(authTokenPair);

			// when
			AuthResult result = authService.loginEmailUser(EMAIL, PASSWORD);

			// then
			assertThat(result).isNotNull();
			assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);

			// verify
			verify(authValidator).validateCredential(user, PASSWORD);
			verify(userValidator).validateUserStatusForLogin(user);
			verify(authTokenManager).issueTokens(user, null);
		}

		@Nested
		@DisplayName("이메일 로그인 실패")
		class LoginFailTest {

			@Test
			@DisplayName("실패: 존재하지 않는 이메일")
			void fail_user_not_found() {
				// given
				given(userReader.findByEmailOrElseThrow(EMAIL))
					.willThrow(UserErrorCode.INVALID_LOGIN.toBaseException());

				// when & then
				assertThatThrownBy(() -> authService.loginEmailUser(EMAIL, PASSWORD))
					.isInstanceOf(BaseRunTimeV2Exception.class)
					.hasMessage(UserErrorCode.INVALID_LOGIN.getMessage());

				// verify
				verify(authValidator, never()).validateCredential(any(), any());
			}

			@Test
			@DisplayName("실패: 비밀번호 불일치")
			void fail_password_mismatch() {
				// given
				given(userReader.findByEmailOrElseThrow(EMAIL)).willReturn(user);

				doThrow(UserErrorCode.INVALID_LOGIN.toBaseException())
					.when(authValidator).validateCredential(user, PASSWORD);

				// when & then
				assertThatThrownBy(() -> authService.loginEmailUser(EMAIL, PASSWORD))
					.isInstanceOf(BaseRunTimeV2Exception.class)
					.hasMessage(UserErrorCode.INVALID_LOGIN.getMessage());

				// verify
				verify(authTokenManager, never()).issueTokens(any(), any());
			}

			@ParameterizedTest(name = "실패: 사용자 상태 오류 ({0})")
			@EnumSource(value = UserErrorCode.class, names = {
				"INVALID_LOGIN_USER_DELETED",
				"INVALID_LOGIN_USER_DROPPED",
				"INVALID_LOGIN_USER_INACTIVE"
			})
			void fail_invalid_user_state(UserErrorCode errorCode) {
				// given
				given(userReader.findByEmailOrElseThrow(EMAIL)).willReturn(user);

				doThrow(errorCode.toBaseException())
					.when(authValidator).validateCredential(user, PASSWORD);

				// when & then
				assertThatThrownBy(() -> authService.loginEmailUser(EMAIL, PASSWORD))
					.isInstanceOf(BaseRunTimeV2Exception.class)
					.hasMessage(errorCode.getMessage());
			}
		}

		@Nested
		@DisplayName("토큰 재발급 (updateToken)")
		class UpdateTokenTest {

			@Test
			@DisplayName("성공: 유효한 리프레시 토큰으로 요청 시 새로운 토큰 쌍을 반환한다.")
			void success() {
				// given
				AuthTokenPair newTokens = new AuthTokenPair(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN);

				given(authTokenManager.getUserIdFromRefreshToken(REFRESH_TOKEN)).willReturn(USER_ID);
				given(userReader.findUserById(USER_ID)).willReturn(user);
				given(authTokenManager.issueTokens(user, REFRESH_TOKEN)).willReturn(newTokens);

				// when
				AuthResult result = authService.updateToken(REFRESH_TOKEN);

				// then
				assertThat(result.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
				assertThat(result.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);

				// verify
				verify(userValidator).validateUser(user);
				verify(authTokenManager).issueTokens(user, REFRESH_TOKEN);
			}

			@Test
			@DisplayName("실패: 리프레시 토큰이 null인 경우 에러를 반환한다.")
			void fail_token_missing() {
				// when & then
				assertThatThrownBy(() -> authService.updateToken(null))
					.isInstanceOf(BaseRunTimeV2Exception.class)
					.hasMessage(AuthErrorCode.REFRESH_TOKEN_MISSING.getMessage());

				verify(authTokenManager, never()).issueTokens(any(), any());
			}

			@Test
			@DisplayName("실패: 유효하지 않은 리프레시 토큰(Redis 없음)인 경우 에러를 반환한다.")
			void fail_invalid_token() {
				// given
				given(authTokenManager.getUserIdFromRefreshToken(REFRESH_TOKEN))
					.willThrow(AuthErrorCode.INVALID_REFRESH_TOKEN.toBaseException());

				// when & then
				assertThatThrownBy(() -> authService.updateToken(REFRESH_TOKEN))
					.isInstanceOf(BaseRunTimeV2Exception.class)
					.hasMessage(AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage());
			}
		}

		@Nested
		@DisplayName("로그아웃 (signOut)")
		class SignOutTest {

			@Test
			@DisplayName("성공: FCM 토큰이 있는 경우 FCM 삭제 및 토큰 무효화를 수행한다.")
			void success_with_fcm() {
				// given
				given(userReader.findUserById(USER_ID)).willReturn(user);

				// when
				authService.signOut(USER_ID, authTokenPair, FCM_TOKEN);

				// verify
				verify(userReader).findUserById(USER_ID);
				verify(userPushTokenWriter).removeFcmToken(user, FCM_TOKEN);
				verify(authTokenManager).invalidateTokens(ACCESS_TOKEN, REFRESH_TOKEN);
			}

			@Test
			@DisplayName("성공: FCM 토큰이 없는 경우(null) 토큰 무효화만 수행한다.")
			void success_without_fcm() {
				// when
				authService.signOut(USER_ID, authTokenPair, null);

				// verify
				verify(userReader, never()).findUserById(anyString());
				verify(userPushTokenWriter, never()).removeFcmToken(any(), anyString());
				verify(authTokenManager).invalidateTokens(ACCESS_TOKEN, REFRESH_TOKEN);
			}
		}
	}
}