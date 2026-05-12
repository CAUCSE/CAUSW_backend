package net.causw.app.main.domain.user.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerWriter;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserWithdrawResponse;
import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoRepository;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountReader;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountUnlinkManager;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.AuthValidator;
import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.domain.user.terms.service.implementation.TermsReader;
import net.causw.app.main.domain.user.terms.service.implementation.TermsValidator;
import net.causw.app.main.domain.user.terms.service.implementation.UserTermsAgreementWriter;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.TermsErrorCode;
import net.causw.app.main.shared.infra.firebase.FcmUtils;

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
	private AuthValidator authValidator;

	@Mock
	private AuthTokenManager authTokenManager;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private TermsReader termsReader;

	@Mock
	private TermsValidator termsValidator;

	@Mock
	private UserTermsAgreementWriter userTermsAgreementWriter;

	@Mock
	private UserInfoRepository userInfoRepository;

	@Mock
	private SocialAccountWriter socialAccountWriter;

	@Mock
	private SocialAccountReader socialAccountReader;

	@Mock
	private SocialAccountUnlinkManager socialAccountUnlinkManager;

	@Mock
	private LockerReader lockerReader;

	@Mock
	private LockerWriter lockerWriter;

	@Mock
	private FcmUtils fcmUtils;

	@Mock
	private UserProfileImageService userProfileImageService;

	private final String userId = "test-uuid";
	private final String nickname = "푸앙";
	private final String phoneNumber = "01012345678";
	private final String name = "홍길동";
	private final String refreshToken = "old-refresh-token";
	private final String platformHint = "ios";
	private final List<String> agreedTermsIds = List.of("term-id-1", "term-id-2");

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
		doNothing().when(termsValidator).validateForAgreement(anyList());
		when(termsReader.findAllById(agreedTermsIds)).thenReturn(List.of(mock(Terms.class), mock(Terms.class)));

		//when
		AuthResult result = userAccountService.completeRegistration(userId, nickname, phoneNumber, name, agreedTermsIds,
			refreshToken);

		//then
		assertNotNull(result);
		assertEquals("new-access-token", result.accessToken());

		//verify
		verify(userReader).findUserById(userId);
		verify(userValidator).checkNicknameDuplication(nickname);
		verify(userValidator).checkPhoneNumDuplication(phoneNumber);
		verify(guestUser).submitRegistration(name, nickname, phoneNumber);
		verify(userWriter).save(guestUser);
		verify(userTermsAgreementWriter).saveAll(any());
		verify(authTokenManager).issueTokens(guestUser, refreshToken);
	}

	@Test
	@DisplayName("필수 약관 미동의 시 실패")
	void completeRegistration_Fail_TermsNotAgreed() {
		User guestUser = mock(User.class);
		when(userReader.findUserById(userId)).thenReturn(guestUser);
		when(guestUser.getState()).thenReturn(UserState.GUEST);
		doThrow(TermsErrorCode.NOT_ALL_REQUIRED_TERMS_AGREED.toBaseException())
			.when(termsValidator)
			.validateForAgreement(anyList());

		BaseRunTimeV2Exception ex = assertThrows(BaseRunTimeV2Exception.class,
			() -> userAccountService.completeRegistration(userId, nickname, phoneNumber, name, agreedTermsIds,
				refreshToken));

		assertEquals(TermsErrorCode.NOT_ALL_REQUIRED_TERMS_AGREED.getMessage(), ex.getMessage());
		verifyNoInteractions(termsReader);
		verifyNoInteractions(userTermsAgreementWriter);
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
			() -> userAccountService.completeRegistration(userId, nickname, phoneNumber, name, agreedTermsIds,
				refreshToken));

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

	@Test
	@DisplayName("일반 회원 탈퇴 성공 - soft delete 및 부가 처리 수행")
	void withdraw_Success() {
		// given
		String accessToken = "access-token";
		String refresh = "refresh-token";
		LocalDateTime now = LocalDateTime.now();

		User user = mock(User.class);
		Locker locker = mock(Locker.class);
		SocialAccount socialAccount = mock(SocialAccount.class);

		when(userReader.findUserById(userId)).thenReturn(user);
		when(user.getId()).thenReturn(userId);
		when(user.isDeleted()).thenReturn(false);
		when(user.getState()).thenReturn(UserState.ACTIVE);

		// 소셜 계정 목록 반환
		when(socialAccountReader.findAllByUserId(userId)).thenReturn(List.of(socialAccount));
		when(lockerReader.findByUserId(userId)).thenReturn(Optional.of(locker));
		when(user.getDeletedAt()).thenReturn(now);

		UserWithdrawResponse result = userAccountService.withdraw(userId, accessToken, refresh, platformHint);

		// then
		assertNotNull(result);
		assertEquals(now, result.deletedAt());

		// verify
		verify(userProfileImageService).requestProfileImageDeletionForWithdrawal(userId);
		verify(userReader).findUserById(userId);
		verify(socialAccountReader).findAllByUserId(userId);
		verify(socialAccountUnlinkManager).unlink(socialAccount, platformHint);
		verify(authTokenManager).invalidateTokens(accessToken, refresh);
		verify(lockerReader).findByUserId(userId);
		verify(lockerWriter).returnLocker(locker, user);
		verify(fcmUtils).clearFcmTokens(user);

		verify(userWriter).withdraw(user);
	}

	@Test
	@DisplayName("사물함이 없는 회원도 탈퇴할 수 있다")
	void withdraw_Success_WithoutLocker() {
		// given
		String accessToken = "access-token";
		String refresh = "refresh-token";
		String platformHint = null;
		LocalDateTime now = LocalDateTime.now();

		User user = mock(User.class);
		when(userReader.findUserById(userId)).thenReturn(user);
		when(user.getId()).thenReturn(userId);
		when(user.isDeleted()).thenReturn(false);
		when(user.getState()).thenReturn(UserState.ACTIVE);

		when(socialAccountReader.findAllByUserId(userId)).thenReturn(List.of());
		when(lockerReader.findByUserId(userId)).thenReturn(Optional.empty());
		when(user.getDeletedAt()).thenReturn(now);

		UserWithdrawResponse result = userAccountService.withdraw(userId, accessToken, refresh, platformHint);

		// then
		assertNotNull(result);
		assertEquals(now, result.deletedAt());
		verify(userProfileImageService).requestProfileImageDeletionForWithdrawal(userId);
		verify(lockerWriter, never()).returnLocker(any(), any());
		verify(userWriter).withdraw(user);
	}

	@Test
	@DisplayName("이미 탈퇴한 회원은 다시 탈퇴할 수 없다")
	void withdraw_Fail_AlreadyDeleted() {
		// given
		User user = mock(User.class);
		when(userReader.findUserById(userId)).thenReturn(user);
		when(user.isDeleted()).thenReturn(true);

		// when & then
		assertThrows(BaseRunTimeV2Exception.class,
			() -> userAccountService.withdraw(userId, "access-token", "refresh-token", platformHint));

		verify(userReader).findUserById(userId);
		verifyNoInteractions(socialAccountReader, socialAccountUnlinkManager, lockerReader, lockerWriter, fcmUtils);
	}
}
