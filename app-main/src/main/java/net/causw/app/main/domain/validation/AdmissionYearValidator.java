package net.causw.app.main.domain.validation;

import java.util.Calendar;

import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class AdmissionYearValidator extends AbstractValidator {

	private final Integer admissionYear;

	private AdmissionYearValidator(Integer admissionYear) {
		this.admissionYear = admissionYear;
	}

	public static AdmissionYearValidator of(Integer admissionYear) {
		return new AdmissionYearValidator(admissionYear);
	}

	@Override
	public void validate() {
		if (!this.validateAdmissionYear()) {
			throw new BadRequestException(
				ErrorCode.INVALID_USER_DATA_REQUEST,
				"입학년도를 다시 확인해주세요."
			);
		}
	}

	public boolean validateAdmissionYear() {
		if (this.admissionYear < StaticValue.CAUSW_CREATED) {
			return false;
		}

		Calendar cal = Calendar.getInstance();
		int presentYear = cal.get(Calendar.YEAR);
		return this.admissionYear <= presentYear;
	}
}
