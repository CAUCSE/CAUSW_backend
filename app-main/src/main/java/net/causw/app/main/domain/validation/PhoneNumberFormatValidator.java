package net.causw.app.main.domain.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class PhoneNumberFormatValidator extends AbstractValidator {

	private final String phoneNumber;

	private PhoneNumberFormatValidator(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public static PhoneNumberFormatValidator of(String phoneNumber) {
		return new PhoneNumberFormatValidator(phoneNumber);
	}

	@Override
	public void validate() {
		if (!this.validatePhoneNumber()) {
			throw new BadRequestException(
				ErrorCode.INVALID_USER_DATA_REQUEST,
				"전화번호 형식이 잘못되었습니다."
			);
		}
	}

	public boolean validatePhoneNumber() {
		String phoneNumberPolicy = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$";
		Pattern pattern_phoneNumber = Pattern.compile(phoneNumberPolicy);
		Matcher matcher_phoneNumber = pattern_phoneNumber.matcher(this.phoneNumber);

		return matcher_phoneNumber.matches();
	}
}