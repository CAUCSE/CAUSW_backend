package net.causw.app.main.domain.user.account.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.UserPasswordUpdateCommand;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.account.service.v1.PasswordGenerator;
import net.causw.app.main.domain.user.auth.entity.EmailVerification;
import net.causw.app.main.domain.user.auth.entity.EmailVerification.VerificationStatus;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.AuthValidator;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationReader;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationSender;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationValidator;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationWriter;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAccountService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UserValidator userValidator;
	private final AuthValidator authValidator;
	private final AuthTokenManager authTokenManager;
	private final PasswordEncoder passwordEncoder;
	private final PasswordGenerator passwordGenerator;
	private final EmailVerificationSender emailVerificationSender;
	private final EmailVerificationWriter emailVerificationWriter;
	private final EmailVerificationReader emailVerificationReader;
	private final EmailVerificationValidator emailVerificationValidator;

	/**
	 * 소셜 로그인을 통해 생성된 임시 유저(GUEST)의 추가 정보를 등록하고 회원가입 절차를 완료합니다.
	 * <p>
	 * 이 과정에서 닉네임, 전화번호 등의 중복 검사를 수행하며,
	 * 성공 시 유저의 상태를 GUEST에서 AWAIT(승인 대기)으로 변경하고 새로운 인증 토큰을 발급합니다.
	 * </p>
	 *
	 * @param userId       정보를 등록할 유저의 고유 식별자 (PK)
	 * @param nickname     사용할 닉네임 (중복 검사 대상)
	 * @param phoneNumber  사용할 전화번호 (중복 검사 대상)
	 * @param name         사용자의 실명(이름)
	 * @param refreshToken 갱신할 기존 리프레시 토큰
	 * @return {@link AuthResult} 변경된 권한이 반영된 새로운 토큰 세트와 유저 정보
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_REGISTRATION_STATUS] 유저 상태가 GUEST가 아닌 경우,
	 * 닉네임 또는 전화번호가 중복된 경우
	 */
	@Transactional
	public AuthResult completeRegistration(String userId, String nickname, String phoneNumber, String name,
		String refreshToken) {
		User guestUser = userReader.findUserById(userId);
		if (guestUser.getState() != UserState.GUEST) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}
		userValidator.checkNicknameDuplication(nickname);
		userValidator.checkPhoneNumDuplication(phoneNumber);
		guestUser.submitRegistration(name, nickname, phoneNumber);
		User updatedUser = userWriter.save(guestUser);
		AuthTokenPair tokens = authTokenManager.issueTokens(updatedUser, refreshToken);
		return AuthResult.of(tokens.accessToken(), updatedUser.getName(), updatedUser.getEmail(),
			updatedUser.getProfileUrl(),
			tokens.refreshToken());
	}

	/**
	 * 현재 로그인한 사용자의 닉네임을 변경합니다.
	 * <p>
	 * 현재 닉네임과 동일한 값이면 예외를 발생시키고,
	 * 중복 닉네임 검사 후 성공 시 닉네임을 저장합니다.
	 * </p>
	 *
	 * @param userId   닉네임을 변경할 사용자의 고유 식별자 (PK)
	 * @param nickname 변경할 닉네임
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [NICKNAME_SAME_AS_CURRENT] 현재 닉네임과 동일한 경우,
	 * [NICKNAME_ALREADY_EXIST] 이미 사용 중인 닉네임인 경우
	 */
	@Transactional
	public void updateNickname(String userId, String nickname) {
		User user = userReader.findUserById(userId);
		if (nickname.equals(user.getNickname())) {
			throw UserErrorCode.NICKNAME_SAME_AS_CURRENT.toBaseException();
		}
		userValidator.checkNicknameDuplication(nickname);
		user.updateNickname(nickname);
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

	/**
	 * 현재 비밀번호를 확인하고 새 비밀번호로 변경합니다.
	 * <p>
	 * 소셜 로그인 전용 계정(비밀번호 없음)은 변경할 수 없으며,
	 * 현재 비밀번호 일치 여부, 새 비밀번호 형식, 새 비밀번호 확인 일치 여부를 검사합니다.
	 * </p>
	 *
	 * @param userId             비밀번호를 변경할 유저의 고유 식별자 (PK)
	 * @param command			비밀번호 재설정 command
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [SOCIAL_USER_CANNOT_CHANGE_PASSWORD] 소셜 로그인 전용 계정인 경우,
	 * [INVALID_CURRENT_PASSWORD] 현재 비밀번호가 일치하지 않는 경우,
	 * [INVALID_PASSWORD_REQUEST] 새 비밀번호 형식이 잘못된 경우,
	 * [PASSWORD_CONFIRM_MISMATCH] 새 비밀번호와 확인 값이 일치하지 않는 경우
	 */
	@Transactional
	public void updatePassword(String userId, UserPasswordUpdateCommand command) {
		User user = userReader.findUserById(userId);

		if (user.isOnlySocialUser()) {
			throw UserErrorCode.SOCIAL_ONLY_USER_CANNOT_CHANGE_PASSWORD.toBaseException();
		}

		if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
			throw UserErrorCode.INVALID_CURRENT_PASSWORD.toBaseException();
		}

		if (!command.newPassword().equals(command.newPasswordConfirm())) {
			throw UserErrorCode.PASSWORD_CONFIRM_MISMATCH.toBaseException();
		}

		authValidator.validatePasswordFormat(command.newPassword());

		user.updatePassword(passwordEncoder.encode(command.newPassword()));
	}

	/**
	 * 이름+이메일 기준으로 비밀번호 초기화용 인증코드를 발송합니다.
	 *
	 * @param name  사용자 이름
	 * @param email 인증 코드를 받을 이메일 주소
	 */
	@Transactional
	public void sendPasswordResetVerificationEmail(String name, String email) {
		emailVerificationValidator.validatePasswordResetSend(name, email);
		emailVerificationSender.send(email, VerificationStatus.PASSWORD_FIND);
	}

	/**
	 * 비밀번호 초기화 인증번호를 검증하고 임시 비밀번호로 재설정합니다.
	 * 검증 성공 시 해당 인증 레코드는 삭제됩니다.
	 *
	 * @param name             사용자 이름
	 * @param email            인증할 이메일 주소
	 * @param verificationCode 사용자가 입력한 인증 코드
	 * @return 화면에 표시할 임시 비밀번호
	 */
	@Transactional
	public String resetPasswordByVerificationCode(String name, String email, String verificationCode) {
		User user = userReader.findByEmailAndName(email, name);

		if (user.isOnlySocialUser()) {
			throw UserErrorCode.SOCIAL_ONLY_USER_CANNOT_CHANGE_PASSWORD.toBaseException();
		}

		EmailVerification emailVerification = emailVerificationReader.findLatestByEmailAndStatus(email,
			VerificationStatus.PASSWORD_FIND);

		if (emailVerification.isExpired()) {
			throw UserErrorCode.PASSWORD_RESET_EXPIRED.toBaseException();
		}

		if (!emailVerification.getVerificationCode().equals(verificationCode)) {
			throw UserErrorCode.PASSWORD_RESET_CODE_MISMATCH.toBaseException();
		}

		emailVerificationWriter.delete(emailVerification);

		String temporaryPassword = passwordGenerator.generate();
		user.updatePassword(passwordEncoder.encode(temporaryPassword));
		userWriter.save(user);
		return temporaryPassword;
	}

}
