package net.causw.app.main.domain.user.account.util;

import net.causw.app.main.domain.user.account.policy.PasswordPolicy;
import net.causw.app.main.shared.AbstractValidator;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class PasswordFormatValidator extends AbstractValidator {

	private final String password;

	private PasswordFormatValidator(String password) {
		this.password = password;
	}

	public static PasswordFormatValidator of(String password) {
		return new PasswordFormatValidator(password);
	}

	@Override
	public void validate() {
		if (!this.validatePassword()) {
			throw new BadRequestException(
				ErrorCode.INVALID_USER_DATA_REQUEST,
				"비밀번호 형식이 잘못되었습니다.");
		}
	}

	public boolean validatePassword() {
		return PasswordPolicy.matches(this.password);
	}
}
