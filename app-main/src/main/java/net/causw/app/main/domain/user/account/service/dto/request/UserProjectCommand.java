package net.causw.app.main.domain.user.account.service.dto.request;

import java.time.LocalDate;

import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

public record UserProjectCommand(
	String id,
	int startYear,
	int startMonth,
	Integer endYear,
	Integer endMonth,
	String description) {
	public void validateDate() {
		int currentYear = LocalDate.now().getYear();

		if (startYear > currentYear) {
			throw UserInfoErrorCode.INVALID_PROJECT_START_DATE.toBaseException();
		}

		if ((endYear == null) != (endMonth == null)) {
			throw UserInfoErrorCode.INVALID_PROJECT_END_DATE.toBaseException();
		}

		if (endYear != null) {
			if (endYear > currentYear) {
				throw UserInfoErrorCode.INVALID_PROJECT_END_DATE.toBaseException();
			}
			if (startYear == endYear && endMonth < startMonth) {
				throw UserInfoErrorCode.PROJECT_START_BEFORE_END.toBaseException();
			}
		}
	}
}
