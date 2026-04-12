package net.causw.app.main.domain.user.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoRepository;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.AuthValidator;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
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
	private UserInfoRepository userInfoRepository;

	@Mock
	private SocialAccountWriter socialAccountWriter;

	@Mock
	private LockerReader lockerReader;

	@Mock
	private LockerWriter lockerWriter;

	@Mock
	private FcmUtils fcmUtils;

	private final String userId = "test-uuid";
	private final String nickname = "푸앙";
	private final String phoneNumber = "01012345678";
	private final String name = "홍길동";
	private final String refreshToken = "old-refresh-token";

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

	@Test
	@DisplayName("일반 회원 탈퇴 성공 - soft delete 및 부가 처리 수행")
	void withdraw_Success() {
		// given
		String accessToken = "access-token";
		String refresh = "refresh-token";

		User user = mock(User.class);
		Locker locker = mock(Locker.class);
		AtomicReference<LocalDateTime> deletedAtRef = new AtomicReference<>();

		when(userReader.findUserById(userId)).thenReturn(user);
		when(user.isDeleted()).thenReturn(false);
		when(user.getState()).thenReturn(UserState.ACTIVE);
		when(user.getId()).thenReturn(userId);
		when(lockerReader.findByUserId(userId)).thenReturn(Optional.of(locker));
		when(user.getDeletedAt()).thenAnswer(invocation -> deletedAtRef.get());

		doAnswer(invocation -> {
			LocalDateTime deletedAt = invocation.getArgument(0);
			deletedAtRef.set(deletedAt);
			return null;
		}).when(user).withdraw(any(LocalDateTime.class));

		// when
		UserWithdrawResponse result = userAccountService.withdraw(userId, accessToken, refresh);

		// then
		assertNotNull(result);
		assertNotNull(deletedAtRef.get());

		verify(userReader).findUserById(userId);
		verify(socialAccountWriter).unlinkAllByUser(user);
		verify(authTokenManager).invalidateTokens(accessToken, refresh);
		verify(lockerReader).findByUserId(userId);
		verify(lockerWriter).returnLocker(locker, user);
		verify(fcmUtils).clearFcmTokens(user);
		verify(user).withdraw(any(LocalDateTime.class));
		verify(userWriter).save(user);
	}

	@Test
	@DisplayName("사물함이 없는 회원도 탈퇴할 수 있다")
	void withdraw_Success_WithoutLocker() {
		// given
		String accessToken = "access-token";
		String refresh = "refresh-token";

		User user = mock(User.class);
		AtomicReference<LocalDateTime> deletedAtRef = new AtomicReference<>();

		when(userReader.findUserById(userId)).thenReturn(user);
		when(user.isDeleted()).thenReturn(false);
		when(user.getState()).thenReturn(UserState.ACTIVE);
		when(user.getId()).thenReturn(userId);
		when(lockerReader.findByUserId(userId)).thenReturn(Optional.empty());
		when(user.getDeletedAt()).thenAnswer(invocation -> deletedAtRef.get());

		doAnswer(invocation -> {
			LocalDateTime deletedAt = invocation.getArgument(0);
			deletedAtRef.set(deletedAt);
			return null;
		}).when(user).withdraw(any(LocalDateTime.class));

		// when
		UserWithdrawResponse result = userAccountService.withdraw(userId, accessToken, refresh);

		// then
		assertNotNull(result);
		assertNotNull(deletedAtRef.get());

		verify(userReader).findUserById(userId);
		verify(socialAccountWriter).unlinkAllByUser(user);
		verify(authTokenManager).invalidateTokens(accessToken, refresh);
		verify(lockerReader).findByUserId(userId);
		verify(lockerWriter, never()).returnLocker(any(), any());
		verify(fcmUtils).clearFcmTokens(user);
		verify(user).withdraw(any(LocalDateTime.class));
		verify(userWriter).save(user);
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
			() -> userAccountService.withdraw(userId, "access-token", "refresh-token"));

		verify(userReader).findUserById(userId);
		verifyNoInteractions(socialAccountWriter, lockerReader, lockerWriter, fcmUtils);
	}
}
