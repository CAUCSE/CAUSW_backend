package net.causw.app.main.domain.user.auth.service.v2;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.causw.app.main.core.security.JwtTokenProvider;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.dto.UserRegisterDto;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.api.v2.dto.AuthDtoMapper;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.service.v2.dto.AuthInternalDto;
import net.causw.app.main.domain.user.auth.util.EmailUserValidator;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.shared.infra.redis.RedisUtils;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@InjectMocks
	AuthService authService;

	@Mock
	private UserReader userReader;
	@Mock
	private UserWriter userWriter;
	@Mock
	private EmailUserValidator emailUserValidator;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private AuthDtoMapper authDtoMapper;
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private RedisUtils redisUtils;

	private static final String EMAIL = "test@example.com";
	private static final String PASSWORD = "password1234";
	private static final String ENCODED_PASSWORD = "encodedPassword";
	private static final String NAME = "TestUser";
	private static final String NICKNAME = "Nick";
	private static final String PHONE = "010-1234-5678";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = "refresh_token";

	private UserRegisterDto registerDto;
	private User user;
	private AuthResponse authResponse;

	@BeforeEach
	void setup() {
		registerDto = new UserRegisterDto(EMAIL, PASSWORD, NAME, NICKNAME, PHONE);
		user = User.from(registerDto, ENCODED_PASSWORD);
		authResponse = AuthResponse.builder()
			.email(EMAIL)
			.name(NAME)
			.accessToken(ACCESS_TOKEN)
			.build();
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
			given(authDtoMapper.toAuthResponse(any(User.class), any(), any())).willReturn(authResponse);

			// when
			AuthResponse response = authService.registerEmailUser(registerDto);

			// then
			assertThat(response).isNotNull();
			assertThat(response.email()).isEqualTo(EMAIL);

			// verify
			verify(userReader).checkEmailDuplication(EMAIL);
			verify(userReader).checkNicknameDuplication(NICKNAME);
			verify(userReader).checkPhoneNumDuplication(PHONE);
			verify(emailUserValidator).validateRegister(any(User.class), eq(PASSWORD), eq(PHONE));
			verify(userWriter).save(any(User.class));
		}

		@Nested
		@DisplayName("회원가입 실패 케이스")
		class RegisterFailTest {

			@Nested
			@DisplayName("실패: 이미 등록된 사용자(이름/전화번호)가 있고, 가입 불가능한 상태라면 사용자 상태에 따라 에러를 반환한다.")
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
						.when(mockedExistingUser).validateSignUpPossible();

					// when & then
					assertThrows(BaseRunTimeV2Exception.class, () -> authService.registerEmailUser(registerDto));

					// verify
					verify(userReader).checkUserExistByPhoneNumAndName(PHONE, NAME);
					verify(mockedExistingUser).validateSignUpPossible();
					verify(userReader, never()).checkEmailDuplication(anyString());
				}
			}

			@Test
			@DisplayName("실패: 이메일이 중복되면 에러를 반환한다.")
			void fail_email_duplicate() {
				// given
				given(userReader.checkUserExistByPhoneNumAndName(anyString(), anyString()))
					.willReturn(Optional.empty());

				doThrow(UserErrorCode.EMAIL_ALREADY_EXIST.toBaseException())
					.when(userReader).checkEmailDuplication(EMAIL);

				// when & then
				assertThrows(BaseRunTimeV2Exception.class, () -> authService.registerEmailUser(registerDto));

				// verify
				verify(userReader).checkEmailDuplication(EMAIL);
				verify(userReader, never()).checkNicknameDuplication(anyString());
			}

			@Test
			@DisplayName("실패: 닉네임이 중복되면 에러를 반환한다.")
			void fail_nickname_duplicate() {
				// given
				given(userReader.checkUserExistByPhoneNumAndName(anyString(), anyString()))
					.willReturn(Optional.empty());

				doThrow(UserErrorCode.NICKNAME_ALREADY_EXIST.toBaseException())
					.when(userReader).checkNicknameDuplication(NICKNAME);

				// when & then
				assertThrows(BaseRunTimeV2Exception.class, () -> authService.registerEmailUser(registerDto));

				// verify
				verify(userReader).checkNicknameDuplication(NICKNAME);
				verify(userReader, never()).checkPhoneNumDuplication(anyString());
			}

			@Test
			@DisplayName("실패: 전화번호가 중복되면 에러를 반환한다.")
			void fail_phoneNumber_duplicate() {
				// given
				given(userReader.checkUserExistByPhoneNumAndName(anyString(), anyString()))
					.willReturn(Optional.empty());

				doThrow(UserErrorCode.PHONE_NUMBER_ALREADY_EXIST.toBaseException())
					.when(userReader).checkPhoneNumDuplication(PHONE);

				// when & then
				assertThrows(BaseRunTimeV2Exception.class, () -> authService.registerEmailUser(registerDto));

				// verify
				verify(userReader).checkPhoneNumDuplication(PHONE);
				verify(userWriter, never()).save(any(User.class));
			}

			@Nested
			@DisplayName("데이터 유효성 검증 실패 (Validator Fail)")
			class ValidationFailTest {
				static Stream<Exception> provideValidationExceptions() {
					return Stream.of(
						// Case 1: 비밀번호 양식 오류
						new BadRequestException(ErrorCode.INVALID_USER_DATA_REQUEST, "비밀번호 형식이 잘못되었습니다."),

						// Case 2: 전화번호 양식 오류
						new BadRequestException(ErrorCode.INVALID_USER_DATA_REQUEST, "전화번호 형식이 잘못되었습니다."),

						// Case 3: 객체 검증(Constraint) 오류 (@NotNull, @Size 등)
						new ConstraintViolationException("Validation failed", Set.of()));
				}

				@ParameterizedTest(name = "발생 예외: {0}")
				@MethodSource("provideValidationExceptions")
				void fail_when_validation_throws_exception(Exception exceptionToThrow) {
					// given
					given(userReader.checkUserExistByPhoneNumAndName(anyString(), anyString()))
						.willReturn(Optional.empty());
					given(passwordEncoder.encode(anyString())).willReturn(ENCODED_PASSWORD);

					doThrow(exceptionToThrow)
						.when(emailUserValidator).validateRegister(any(User.class), eq(PASSWORD), eq(PHONE));

					// when & then
					assertThatThrownBy(() -> authService.registerEmailUser(registerDto))
						.isInstanceOf(exceptionToThrow.getClass())
						.hasMessage(exceptionToThrow.getMessage());

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
		@DisplayName("성공: 이메일과 비밀번호가 일치하면 토큰을 발급한다.")
		void success() {
			// given
			given(userReader.findByEmailOrElseThrow(EMAIL)).willReturn(user);
			given(jwtTokenProvider.createAccessToken(any(), any(), any())).willReturn(ACCESS_TOKEN);
			given(jwtTokenProvider.createRefreshToken()).willReturn(REFRESH_TOKEN);
			given(authDtoMapper.toAuthResponse(any(User.class), eq(ACCESS_TOKEN), any())).willReturn(authResponse);

			// when
			AuthInternalDto result = authService.loginEmailUser(EMAIL, PASSWORD);

			// then
			assertThat(result).isNotNull();
			assertThat(result.authResponse().accessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);

			// verify
			verify(emailUserValidator).validateLogin(user, PASSWORD);
			verify(redisUtils).setRefreshTokenData(eq(REFRESH_TOKEN), eq(user.getId()),
				eq(StaticValue.JWT_REFRESH_TOKEN_VALID_TIME));
		}

		@Nested
		@DisplayName("이메일 로그인 실패 (Fail Cases)")
		class LoginFailTest {

			@Test
			@DisplayName("실패: 존재하지 않는 이메일로 로그인 시도 시 에러를 반환한다.")
			void fail_user_not_found() {
				// given
				given(userReader.findByEmailOrElseThrow(EMAIL))
					.willThrow(UserErrorCode.INVALID_LOGIN.toBaseException());

				// when & then
				assertThatThrownBy(() -> authService.loginEmailUser(EMAIL, PASSWORD))
					.isInstanceOf(BaseRunTimeV2Exception.class)
					.hasMessage(UserErrorCode.INVALID_LOGIN.getMessage());

				// verify
				verify(emailUserValidator, never()).validateLogin(any(), any());
				verify(jwtTokenProvider, never()).createAccessToken(any(), any(), any());
			}

			@Test
			@DisplayName("실패: 비밀번호가 일치하지 않으면 에러를 반환한다.")
			void fail_password_mismatch() {
				// given
				given(userReader.findByEmailOrElseThrow(EMAIL)).willReturn(user);

				doThrow(UserErrorCode.INVALID_LOGIN.toBaseException())
					.when(emailUserValidator).validateLogin(user, PASSWORD);

				// when & then
				assertThatThrownBy(() -> authService.loginEmailUser(EMAIL, PASSWORD))
					.isInstanceOf(BaseRunTimeV2Exception.class)
					.hasMessage(UserErrorCode.INVALID_LOGIN.getMessage());

				// verify
				verify(jwtTokenProvider, never()).createAccessToken(any(), any(), any());
			}

			@ParameterizedTest(name = "사용자 상태({0})에 따른 로그인 불가 에러 반환")
			@EnumSource(value = UserErrorCode.class, names = {
				"INVALID_LOGIN_USER_DELETED",
				"INVALID_LOGIN_USER_DROPPED",
				"INVALID_LOGIN_USER_INACTIVE"
			})
			void fail_invalid_user_state(UserErrorCode errorCode) {
				// given
				given(userReader.findByEmailOrElseThrow(EMAIL)).willReturn(user);

				doThrow(errorCode.toBaseException())
					.when(emailUserValidator).validateLogin(user, PASSWORD);

				// when & then
				assertThatThrownBy(() -> authService.loginEmailUser(EMAIL, PASSWORD))
					.isInstanceOf(BaseRunTimeV2Exception.class)
					.hasMessage(errorCode.getMessage());

				// verify
				verify(jwtTokenProvider, never()).createAccessToken(any(), any(), any());
			}
		}
	}
}
