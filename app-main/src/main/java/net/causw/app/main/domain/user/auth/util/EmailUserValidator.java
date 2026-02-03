package net.causw.app.main.domain.user.auth.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.util.PasswordCorrectValidator;
import net.causw.app.main.domain.user.account.util.PasswordFormatValidator;
import net.causw.app.main.domain.user.account.util.PhoneNumberFormatValidator;
import net.causw.app.main.shared.ValidatorBucket;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.shared.util.ConstraintValidator;
import net.causw.global.exception.BaseRuntimeException;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailUserValidator {

	private final Validator validator;
	private final PasswordEncoder passwordEncoder;

	public void validateRegister(User newUser, String password, String phoneNumber) {
		ValidatorBucket.of()
			.consistOf(ConstraintValidator.of(newUser, this.validator))
			.consistOf(PasswordFormatValidator.of(password))
			.consistOf(PhoneNumberFormatValidator.of(phoneNumber))
			.validate();
	}

	public void validateLogin(User user, String password) {
		try {
			ValidatorBucket.of()
				.consistOf(PasswordCorrectValidator.of(
					this.passwordEncoder,
					user.getPassword(),
					password))
				.validate();
		} catch (BaseRuntimeException e) {
			throw UserErrorCode.INVALID_LOGIN.toBaseException();
		}
		user.validateLoginPossible();
	}
}
