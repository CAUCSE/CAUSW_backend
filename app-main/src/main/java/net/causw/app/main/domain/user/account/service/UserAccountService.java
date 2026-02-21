package net.causw.app.main.domain.user.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserRegistrationRequest;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.v2.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.v2.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.v2.implementation.AuthTokenManager;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAccountService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UserValidator userValidator;
	private final AuthTokenManager authTokenManager;

	/**
	 * 소셜 로그인을 통해 생성된 임시 유저(GUEST)의 추가 정보를 등록하고 회원가입 절차를 완료합니다.
	 * <p>
	 * 이 과정에서 이름, 닉네임, 전화번호 등의 중복 검사를 수행하며,
	 * 성공 시 유저의 상태를 GUEST에서 AWAIT(승인 대기)으로 변경하고 새로운 인증 토큰을 발급합니다.
	 * </p>
	 *
	 * @param userId  정보를 등록할 유저의 고유 식별자 (PK)
	 * @param request 이름, 닉네임, 전화번호 등 추가 정보가 담긴 DTO
	 * @return {@link AuthResult} 변경된 권한(ROLE_AWAIT 등)이 반영된 새로운 토큰 세트와 유저 정보
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * 유저 상태가 GUEST가 아니거나(INVALID_REGISTRATION_STATUS), 닉네임/전화번호가 중복된 경우
	 */
	@Transactional
	public AuthResult completeRegistration(String userId, UserRegistrationRequest request, String refreshToken) {
		User guestUser = userReader.findUserById(userId);
		if (guestUser.getState() != UserState.GUEST) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}
		userValidator.checkNicknameDuplication(request.nickname());
		userValidator.checkPhoneNumDuplication(request.phoneNumber());
		guestUser.updateRegisterInformation(request.name(), request.nickname(), request.phoneNumber());
		User updatedUser = userWriter.save(guestUser);
		AuthTokenPair tokens = authTokenManager.issueTokens(updatedUser, refreshToken);
		return AuthResult.of(tokens.accessToken(), updatedUser.getName(), updatedUser.getEmail(),
			updatedUser.getProfileUrl(),
			tokens.refreshToken());
	}

	/**
	 * 사용자가 입력한 닉네임이 DB에 이미 존재하는지 확인합니다.
	 * <p>
	 * 중복된 닉네임이 있을 경우 NICKNAME_DUPLICATED 예외를 발생시킵니다.
	 * </p>
	 *
	 * @param nickname 검사할 닉네임
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 닉네임이 중복된 경우
	 */
	@Transactional(readOnly = true)
	public void checkNicknameDuplication(String nickname) {
		userValidator.checkNicknameDuplication(nickname);
	}

	/**
	 * 사용자가 입력한 전화번호가 DB에 이미 존재하는지 확인합니다.
	 * <p>
	 * 중복된 전화번호가 있을 경우 PHONE_NUMBER_DUPLICATED 예외를 발생시킵니다.
	 * </p>
	 *
	 * @param phoneNumber 검사할 전화번호
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 전화번호가 중복된 경우
	 */
	@Transactional(readOnly = true)
	public void checkPhoneNumDuplication(String phoneNumber) {
		userValidator.checkPhoneNumDuplication(phoneNumber);
	}
}
