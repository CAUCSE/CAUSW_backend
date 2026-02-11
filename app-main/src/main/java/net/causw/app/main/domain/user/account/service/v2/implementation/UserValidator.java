package net.causw.app.main.domain.user.account.service.v2.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {
	private final UserRepository userRepository;

	// 사용자가 가입 (재가입)이 가능한지 확인하는 메서드
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

	public void checkEmailDuplication(String email) {
		Optional<User> emailExist = userRepository.findByEmail(email);
		if (emailExist.isPresent()) {
			throw UserErrorCode.EMAIL_ALREADY_EXIST.toBaseException();
		}
	}

	public void checkPhoneNumDuplication(String phoneNumber) {
		Optional<User> phoneNumExist = userRepository.findByPhoneNumber(phoneNumber);
		if (phoneNumExist.isPresent()) {
			throw UserErrorCode.PHONE_NUMBER_ALREADY_EXIST.toBaseException();
		}
	}

	public void checkNicknameDuplication(String nickname) {
		Optional<User> nicknameExist = userRepository.findByNickname(nickname);
		if (nicknameExist.isPresent()) {
			throw UserErrorCode.NICKNAME_ALREADY_EXIST.toBaseException();
		}
	}
}
