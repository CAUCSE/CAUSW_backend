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

		if (!(1 <= startMonth && startMonth <= 12) || startYear < 1800 || startYear > currentYear) {
			throw UserInfoErrorCode.INVALID_CAREER_START_DATE.toBaseException();
		}
		if (endYear != null && endMonth != null) {
			if (!(1 <= endMonth && endMonth <= 12) || endYear < 1800 || endYear > currentYear) {
				throw UserInfoErrorCode.INVALID_CAREER_END_DATE.toBaseException();
			}
		}

	}
}
