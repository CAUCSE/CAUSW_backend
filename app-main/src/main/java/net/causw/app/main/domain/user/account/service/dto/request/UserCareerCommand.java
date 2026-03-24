package net.causw.app.main.domain.user.account.service.dto.request;

import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

public record UserCareerCommand(
	String id,
	int startYear,
	int startMonth,
	Integer endYear,
	Integer endMonth,
	String description) {
	public void validateDate(int currentYear) {

		if (startYear > currentYear) {
			throw UserInfoErrorCode.INVALID_CAREER_START_DATE.toBaseException();
		}

		if ((endYear == null) != (endMonth == null)) {
			throw UserInfoErrorCode.INVALID_CAREER_END_DATE.toBaseException();
		}

		if (endYear != null) {
			if (endYear > currentYear || endYear < startYear) {
				throw UserInfoErrorCode.CAREER_START_BEFORE_END.toBaseException();
			}
			if (startYear == endYear && endMonth < startMonth) {
				throw UserInfoErrorCode.CAREER_START_BEFORE_END.toBaseException();
			}
		}
	}
}