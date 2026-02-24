package net.causw.app.main.domain.user.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.v2.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.v2.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.v2.implementation.AuthTokenManager;
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
}