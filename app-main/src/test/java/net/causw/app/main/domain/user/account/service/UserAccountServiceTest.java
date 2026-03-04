package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.account.service.v1.PasswordGenerator;
import net.causw.app.main.domain.user.auth.entity.EmailVerification;
import net.causw.app.main.domain.user.auth.entity.EmailVerification.VerificationStatus;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationReader;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationSender;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationValidator;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationWriter;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

	@InjectMocks
	private UserAccountService userAccountService;

	@Mock
	private UserReader userReader;

	@Mock
	private UserWriter userWriter;

	@Mock
	private UserValidator userValidator;

	@Mock
	private AuthTokenManager authTokenManager;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private PasswordGenerator passwordGenerator;

	@Mock
	private EmailVerificationSender emailVerificationSender;

	@Mock
	private EmailVerificationWriter emailVerificationWriter;

	@Mock
	private EmailVerificationReader emailVerificationReader;

	@Mock
	private EmailVerificationValidator emailVerificationValidator;

	private final String userId = "test-uuid";
	private final String nickname = "푸앙";
	private final String phoneNumber = "01012345678";
	private final String name = "홍길동";
	private final String email = "test@cau.ac.kr";
	private final String refreshToken = "old-refresh-token";
	private final String code = "ABC123";

	@Test
	@DisplayName("GUEST 유저의 추가 정보 등록 및 회원가입 완료 성공")
	void completeRegistration_Success() {
		//given
		User guestUser = mock(User.class);
		AuthTokenPair tokenPair = mock(AuthTokenPair.class);

		when(userReader.findUserById(userId)).thenReturn(guestUser);
		when(guestUser.getState()).thenReturn(UserState.GUEST);
		when(userWriter.save(guestUser)).thenReturn(guestUser);
		when(authTokenManager.issueTokens(guestUser, refreshToken)).thenReturn(tokenPair);
		when(tokenPair.accessToken()).thenReturn("new-access-token");
		when(tokenPair.refreshToken()).thenReturn("new-refresh-token");

		//when
		AuthResult result = userAccountService.completeRegistration(userId, nickname, phoneNumber, name, refreshToken);

		//then
		assertNotNull(result);
		assertEquals("new-access-token", result.accessToken());

		//verify
		verify(userReader).findUserById(userId);
		verify(userValidator).checkNicknameDuplication(nickname);
		verify(userValidator).checkPhoneNumDuplication(phoneNumber);
		verify(guestUser).submitRegistration(name, nickname, phoneNumber);
		verify(userWriter).save(guestUser);
		verify(authTokenManager).issueTokens(guestUser, refreshToken);
	}

	@Test
	@DisplayName("유저 상태가 GUEST가 아닌 경우 회원가입 완료 실패")
	void completeRegistration_Fail_InvalidStatus() {
		//given
		User activeUser = mock(User.class);

		when(userReader.findUserById(userId)).thenReturn(activeUser);
		when(activeUser.getState()).thenReturn(UserState.ACTIVE);

		//when
		//then
		assertThrows(BaseRunTimeV2Exception.class,
			() -> userAccountService.completeRegistration(userId, nickname, phoneNumber, name, refreshToken));

		//verify
		verify(userReader).findUserById(userId);
		verify(userValidator, never()).checkNicknameDuplication(anyString());
	}

	@Test
	@DisplayName("닉네임 중복 체크 호출 검증")
	void checkNicknameDuplication_CallValidator() {
		//given

		//when
		userAccountService.checkNicknameDuplication(nickname);

		//then

		//verify
		verify(userValidator).checkNicknameDuplication(nickname);
	}

	@Test
	@DisplayName("전화번호 중복 체크 호출 검증")
	void checkPhoneNumDuplication_CallValidator() {
		//given

		//when
		userAccountService.checkPhoneNumDuplication(phoneNumber);

		//then

		//verify
		verify(userValidator).checkPhoneNumDuplication(phoneNumber);
	}

	@Nested
	@DisplayName("비밀번호 초기화 인증코드 발송")
	class SendPasswordResetVerificationEmail {

		@Test
		@DisplayName("성공: 이름+이메일 검증 후 PASSWORD_FIND 상태로 인증코드를 발송한다")
		void success() {
			// when
			userAccountService.sendPasswordResetVerificationEmail(name, email);

			// then
			verify(emailVerificationValidator).validatePasswordResetSend(name, email);
			verify(emailVerificationSender).send(email, VerificationStatus.PASSWORD_FIND);
		}
	}

	@Nested
	@DisplayName("비밀번호 초기화 인증코드 검증 및 임시 비밀번호 발급")
	class ResetPasswordByVerificationCode {

		@Test
		@DisplayName("성공: 인증코드 검증 후 임시 비밀번호로 재설정하고 반환한다")
		void success() {
			// given
			User user = mock(User.class);
			EmailVerification emailVerification = EmailVerification.of(
				email, code, LocalDateTime.now().plusMinutes(5), VerificationStatus.PASSWORD_FIND);

			given(userReader.findByEmailAndName(email, name)).willReturn(user);
			given(user.isOnlySocialUser()).willReturn(false);
			given(emailVerificationReader.findLatestByEmailAndStatus(email, VerificationStatus.PASSWORD_FIND))
				.willReturn(emailVerification);
			given(passwordGenerator.generate()).willReturn("tmpPass@1234");
			given(passwordEncoder.encode("tmpPass@1234")).willReturn("encoded-temp-password");

			// when
			String temporaryPassword = userAccountService.resetPasswordByVerificationCode(name, email, code);

			// then
			assertThat(temporaryPassword).isEqualTo("tmpPass@1234");
			verify(user).updatePassword("encoded-temp-password");
			verify(userWriter).save(user);
			verify(emailVerificationWriter).delete(emailVerification);
		}

		@Test
		@DisplayName("실패: 소셜 전용 계정은 비밀번호 초기화 불가")
		void failWhenSocialOnlyUser() {
			// given
			User user = mock(User.class);

			given(userReader.findByEmailAndName(email, name)).willReturn(user);
			given(user.isOnlySocialUser()).willReturn(true);

			// when & then
			assertThatThrownBy(() -> userAccountService.resetPasswordByVerificationCode(name, email, code))
				.isInstanceOf(BaseRunTimeV2Exception.class);
		}

		@Test
		@DisplayName("실패: 인증 코드 불일치 시 예외 발생")
		void failWhenCodeMismatch() {
			// given
			User user = mock(User.class);
			EmailVerification emailVerification = EmailVerification.of(
				email, code, LocalDateTime.now().plusMinutes(5), VerificationStatus.PASSWORD_FIND);

			given(userReader.findByEmailAndName(email, name)).willReturn(user);
			given(user.isOnlySocialUser()).willReturn(false);
			given(emailVerificationReader.findLatestByEmailAndStatus(email, VerificationStatus.PASSWORD_FIND))
				.willReturn(emailVerification);

			// when & then
			assertThatThrownBy(() -> userAccountService.resetPasswordByVerificationCode(name, email, "ZZZZZZ"))
				.isInstanceOf(BaseRunTimeV2Exception.class);
		}

		@Test
		@DisplayName("실패: 인증 유효 시간 만료 시 예외 발생")
		void failWhenExpired() {
			// given
			User user = mock(User.class);
			EmailVerification expiredVerification = EmailVerification.of(
				email, code, LocalDateTime.now().minusMinutes(1), VerificationStatus.PASSWORD_FIND);

			given(userReader.findByEmailAndName(email, name)).willReturn(user);
			given(user.isOnlySocialUser()).willReturn(false);
			given(emailVerificationReader.findLatestByEmailAndStatus(email, VerificationStatus.PASSWORD_FIND))
				.willReturn(expiredVerification);

			// when & then
			assertThatThrownBy(() -> userAccountService.resetPasswordByVerificationCode(name, email, code))
				.isInstanceOf(BaseRunTimeV2Exception.class);
		}
	}
}