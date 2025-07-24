package net.causw.app.main.domain.validation;

import java.util.Calendar;
import java.util.Set;
import java.util.regex.Pattern;

import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class AdmissionYearsValidator extends AbstractValidator {
	private static final Pattern ADMISSION_YEAR_PATTERN = Pattern.compile("^(19\\d{2}|20\\d{2})$");
	private final Set<Integer> admissionYears;

	private AdmissionYearsValidator(Set<Integer> admissionYears) {
		this.admissionYears = admissionYears;
	}

	public static AdmissionYearsValidator of(Set<Integer> admissionYears) {
		return new AdmissionYearsValidator(admissionYears);
	}

	public void validate() {
		for (Integer year : this.admissionYears) {
			if (!this.validateAdmissionYear(year)) {
				throw new BadRequestException(
					ErrorCode.INVALID_USER_DATA_REQUEST,
					"입학년도를 다시 확인해주세요: " + year
				);
			}
		}
	}

	private boolean validateAdmissionYear(Integer admissionYear) {
		if (!ADMISSION_YEAR_PATTERN.matcher(admissionYear.toString()).matches()) {
			return false;
		}

		if (admissionYear < StaticValue.CAUSW_CREATED) {
			return false;
		}

		Calendar cal = Calendar.getInstance();
		int presentYear = cal.get(Calendar.YEAR);
		return admissionYear <= presentYear;
	}
}
