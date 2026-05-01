package net.causw.app.main.domain.user.account.policy;

import java.util.regex.Pattern;

/**
 * 비밀번호 형식 정책. Bean Validation·서비스 검증·프론트엔드 규칙과 동일해야 합니다.
 */
public final class PasswordPolicy {

	/**
	 * 영문·숫자·특수문자를 각각 포함하고, 영문·숫자·{@code ~!@#$%^&*()_?-}만 사용하며 길이는 8~20자입니다.
	 */
	public static final String REGEX = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[~!@#$%^&*()_?-])[a-zA-Z0-9~!@#$%^&*()_?-]{8,20}$";

	public static final String VALIDATION_MESSAGE = "영문, 숫자, 특수문자를 포함한 8~20자의 비밀번호를 입력해주세요.";

	private static final Pattern PATTERN = Pattern.compile(REGEX);

	private PasswordPolicy() {}

	public static boolean matches(String password) {
		return password != null && PATTERN.matcher(password).matches();
	}
}
