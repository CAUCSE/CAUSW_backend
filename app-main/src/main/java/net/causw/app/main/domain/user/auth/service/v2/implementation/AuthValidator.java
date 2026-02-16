package net.causw.app.main.domain.user.auth.service.v2.implementation;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthValidator {
	private final PasswordEncoder passwordEncoder;

	private static final String PASSWORD_REGEX = "((?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,})";
	private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

	private static final String PHONE_REGEX = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$";
	private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

	/**
	 * 회원가입 시 입력값 포맷 검증
	 */
	public void validateRegisterInput(User newUser, String password, String phoneNumber) {
		validatePasswordFormat(password);
		validatePhoneNumberFormat(phoneNumber);
	}

	/**
	 * 로그인 시 자격 증명 검증
	 */
	public void validateCredential(User user, String inputPassword) {
		if (user.getPassword() == null) {
			throw UserErrorCode.INVALID_LOGIN_SOCIAL_USER.toBaseException();
		}
		if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
			throw UserErrorCode.INVALID_LOGIN.toBaseException();
		}
	}

	private void validatePasswordFormat(String password) {
		if (!PASSWORD_PATTERN.matcher(password).matches()) {
			throw UserErrorCode.INVALID_PASSWORD_REQUEST.toBaseException();
		}
	}

	private void validatePhoneNumberFormat(String phoneNumber) {
		if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
			throw UserErrorCode.INVALID_PHONE_NUMBER_REQUEST.toBaseException();
		}
	}
}
