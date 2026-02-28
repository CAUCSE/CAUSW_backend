package net.causw.app.main.domain.user.account.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.util.PhoneNumberFormatValidator;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.shared.infra.redis.RedisUtils;

import lombok.RequiredArgsConstructor;

/**
 * 사용자의 상태 검증 및 중복 데이터 확인을 담당하는 컴포넌트입니다.
 * <p>
 * 회원가입/로그인 시의 상태(State) 체크, 리프레시 토큰의 소유권 확인,
 * 이메일/전화번호/닉네임의 중복 여부를 검사합니다.
 */
@Component
@RequiredArgsConstructor
public class UserValidator {
	private final UserRepository userRepository;
	private final SocialAccountRepository socialAccountRepository;
	private final RedisUtils redisUtils;

	/**
	 * 회원가입 시도 시, 해당 사용자의 현재 상태를 기반으로 가입 가능 여부를 확인합니다.
	 * <p>
	 * 이미 가입된 유저(ACTIVE, AWAIT, REJECT)이거나 재가입 불가능한 상태인 경우 예외를 발생시킵니다.
	 *
	 * @param state 검사할 사용자 상태
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [ALREADY_REGISTERED] 이미 가입된 회원인 경우,
	 * [USER_DROPPED] 추방된 회원인 경우,
	 * [USER_INACTIVE_CAN_REJOIN] 휴면 계정인 경우 (재가입 절차 필요)
	 */
	public void validateUserStatusForSignup(UserState state) {
		switch (state) {
			case ACTIVE, AWAIT, REJECT ->
				throw UserErrorCode.ALREADY_REGISTERED.toBaseException();
			case DROP ->
				throw UserErrorCode.USER_DROPPED.toBaseException();
			case INACTIVE ->
				throw UserErrorCode.USER_INACTIVE_CAN_REJOIN.toBaseException();
			default -> {}
		}
	}

	/**
	 * 기존 계정에 소셜 계정을 통합(연동)하기 전, 유저의 상태가 유효한지 검증합니다.
	 * <p>
	 * 정상적인 활동 상태(ACTIVE)나 대기 상태인 경우 검증을 통과하며,
	 * 탈퇴 또는 비활성화 상태인 경우 연동을 차단하고 예외를 발생시킵니다.
	 * </p>
	 *
	 * @param state 검증할 유저의 현재 상태
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [USER_DROPPED] 추방된 회원인 경우,
	 * [USER_INACTIVE_CAN_REJOIN] 휴면 계정인 경우 (재가입 절차 필요)
	 */
	public void validateUserStatusForIntegration(UserState state) {
		switch (state) {
			case DROP ->
				throw UserErrorCode.USER_DROPPED.toBaseException();
			case INACTIVE ->
				throw UserErrorCode.USER_INACTIVE_CAN_REJOIN.toBaseException();
			default -> {}
		}
	}

	/**
	 * 로그인 시도 시, 해당 사용자가 로그인이 가능한 상태인지 확인합니다.
	 *
	 * @param state 검사할 사용자 상태
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_LOGIN_USER_DELETED] 탈퇴한 회원,
	 * [INVALID_LOGIN_USER_DROPPED] 추방된 회원,
	 * [INVALID_LOGIN_USER_INACTIVE] 휴면 회원인 경우
	 */
	public void validateUserStatusForLogin(UserState state) {
		switch (state) {
			case DELETED ->
				throw UserErrorCode.INVALID_LOGIN_USER_DELETED.toBaseException();
			case DROP ->
				throw UserErrorCode.INVALID_LOGIN_USER_DROPPED.toBaseException();
			case INACTIVE ->
				throw UserErrorCode.INVALID_LOGIN_USER_INACTIVE.toBaseException();
			default -> {}
		}
	}

	/**
	 * 인증 과정(토큰 재발급 등)에서 유저의 유효성(상태)을 검증합니다.
	 *
	 * @param user 검증할 사용자 엔티티
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [BLOCKED_USER] 추방된 유저, [INACTIVE_USER] 휴면 유저, [DELETED_USER] 탈퇴한 유저인 경우
	 */
	public void validateUser(User user) {
		// 유저 상태 검증
		switch (user.getState()) {
			case DROP ->
				throw AuthErrorCode.DROPPED_USER.toBaseException();
			case INACTIVE ->
				throw AuthErrorCode.INACTIVE_USER.toBaseException();
			case DELETED ->
				throw AuthErrorCode.DELETED_USER.toBaseException();
			default -> {}
		}
	}

	/**
	 * 리프레시 토큰의 유효성과 소유권을 검증합니다.
	 * <p>
	 * Redis에 저장된 토큰 정보와 요청한 사용자 ID가 일치하는지 확인합니다.
	 *
	 * @param userId       검증할 사용자 ID
	 * @param refreshToken 검증할 리프레시 토큰
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_REFRESH_TOKEN] 토큰이 만료되었거나, 토큰의 소유자가 일치하지 않는 경우
	 */
	public void validateRefreshToken(String userId, String refreshToken) {
		String userIdFromRedis = Optional.ofNullable(redisUtils.getRefreshTokenData(refreshToken))
			.orElseThrow(AuthErrorCode.INVALID_REFRESH_TOKEN::toBaseException);

		if (!userId.equals(userIdFromRedis)) {
			throw AuthErrorCode.INVALID_REFRESH_TOKEN.toBaseException();
		}
	}

	/**
	 * 이메일 중복 여부를 확인합니다.
	 *
	 * @param email 검사할 이메일
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [EMAIL_ALREADY_EXIST] 이미 존재하는 이메일인 경우
	 */
	public void checkEmailDuplication(String email) {
		Optional<User> emailExist = userRepository.findByEmail(email);
		if (emailExist.isPresent()) {
			throw UserErrorCode.EMAIL_ALREADY_EXIST.toBaseException();
		}
	}

	/**
	 * 전화번호 중복 여부를 확인합니다.
	 *
	 * @param phoneNumber 검사할 전화번호
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [PHONE_NUMBER_ALREADY_EXIST] 이미 존재하는 전화번호인 경우
	 */
	public void checkPhoneNumDuplication(String phoneNumber) {
		PhoneNumberFormatValidator.of(phoneNumber).validate();
		Optional<User> phoneNumExist = userRepository.findByPhoneNumber(phoneNumber);
		if (phoneNumExist.isPresent()) {
			throw UserErrorCode.PHONE_NUMBER_ALREADY_EXIST.toBaseException();
		}
	}

	/**
	 * 닉네임 중복 여부를 확인합니다.
	 *
	 * @param nickname 검사할 닉네임
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [NICKNAME_ALREADY_EXIST] 이미 존재하는 닉네임인 경우
	 */
	public void checkNicknameDuplication(String nickname) {
		Optional<User> nicknameExist = userRepository.findByNickname(nickname);
		if (nicknameExist.isPresent()) {
			throw UserErrorCode.NICKNAME_ALREADY_EXIST.toBaseException();
		}
	}

	public void checkAccountExistByUserAndSocialType(User user, SocialType socialType) {
		Boolean isExist = socialAccountRepository.existsByUserAndSocialType(user, socialType);
		if (isExist) {
			throw AuthErrorCode.ALREADY_LINKED_SOCIAL_PROVIDER.toBaseException();
		}
	}

	/**
	 * 비밀번호 형식이 정책에 맞는지 검사합니다.
	 * <p>
	 * 영문, 숫자, 특수문자를 각각 1개 이상 포함하고 8자 이상이어야 합니다.
	 *
	 * @param password 검사할 비밀번호
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_PASSWORD_REQUEST] 비밀번호 형식이 정책에 맞지 않는 경우
	 */
	public void validatePasswordFormat(String password) {
		String passwordPolicy = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()-_?]).{8,20}$";
		if (!password.matches(passwordPolicy)) {
			throw UserErrorCode.INVALID_PASSWORD_REQUEST.toBaseException();
		}
	}
}
