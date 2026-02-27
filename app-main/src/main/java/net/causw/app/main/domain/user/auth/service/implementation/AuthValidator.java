package net.causw.app.main.domain.user.auth.service.implementation;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 인증 관련 입력값의 형식 및 자격 증명을 검증하는 컴포넌트입니다.
 * <p>
 * 비밀번호 복잡도 검사, 전화번호 형식 검사, 로그인 시 비밀번호 일치 여부 확인을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class AuthValidator {
	private final PasswordEncoder passwordEncoder;

	private static final String PASSWORD_REGEX = "((?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,})";
	private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

	private static final String PHONE_REGEX = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$";
	private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

	/**
	 * 회원가입 시 입력된 정보의 형식이 유효한지 검증합니다.
	 * <p>
	 * <b>검증 항목:</b>
	 * <ul>
	 * <li>비밀번호: 영문, 숫자, 특수문자를 모두 포함하여 8자 이상이어야 합니다.</li>
	 * <li>전화번호: 010-XXXX-XXXX 형식(하이픈 포함)이어야 합니다.</li>
	 * </ul>
	 *
	 * @param newUser     가입할 사용자 엔티티 (현재 로직에서는 사용되지 않으나 확장을 위해 유지)
	 * @param password    검증할 평문 비밀번호
	 * @param phoneNumber 검증할 전화번호
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_PASSWORD] 비밀번호 형식이 올바르지 않은 경우,
	 * [INVALID_PHONE_NUMBER] 전화번호 형식이 올바르지 않은 경우
	 */
	public void validateRegisterInput(User newUser, String password, String phoneNumber) {
		validatePasswordFormat(password);
		validatePhoneNumberFormat(phoneNumber);
	}

	/**
	 * 로그인 시 입력된 비밀번호가 저장된 사용자의 비밀번호와 일치하는지 검증합니다.
	 *
	 * @param user          DB에서 조회한 사용자 엔티티 (암호화된 비밀번호 포함)
	 * @param inputPassword 사용자가 입력한 평문 비밀번호
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_LOGIN] 비밀번호가 일치하지 않는 경우
	 */
	public void validateCredential(User user, String inputPassword) {
		if (user.isSocialUser()) {
			throw UserErrorCode.INVALID_LOGIN_SOCIAL_USER.toBaseException();
		}
		if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
			throw UserErrorCode.INVALID_LOGIN.toBaseException();
		}
	}

	/**
	 * 비밀번호 포맷 검증 (Private)
	 * Rule: 영문, 숫자, 특수문자 포함 8자 이상
	 */
	private void validatePasswordFormat(String password) {
		if (!PASSWORD_PATTERN.matcher(password).matches()) {
			throw UserErrorCode.INVALID_PASSWORD_REQUEST.toBaseException();
		}
	}

	/**
	 * 전화번호 포맷 검증 (Private)
	 * Rule: 010-XXXX-XXXX (하이픈 포함)
	 */
	private void validatePhoneNumberFormat(String phoneNumber) {
		if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
			throw UserErrorCode.INVALID_PHONE_NUMBER_REQUEST.toBaseException();
		}
	}
}
