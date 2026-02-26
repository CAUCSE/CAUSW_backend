package net.causw.app.main.domain.user.account.service.util;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserCareerDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserProjectDto;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserInfoValidator {
	public void validateUserCareerDate(UserCareerDto userCareerDto) {
		int currentYear = LocalDate.now().getYear();
		int startYear = userCareerDto.startYear();
		int startMonth = userCareerDto.startMonth();
		Integer endYear = userCareerDto.endYear();
		Integer endMonth = userCareerDto.endMonth();

		if (!(1 <= startMonth && startMonth <= 12) || startYear < 1800 || startYear > currentYear) {
			throw UserInfoErrorCode.INVALID_CAREER_START_DATE.toBaseException();
		}

		if (endYear != null && endMonth != null) {
			if (!(1 <= endMonth && endMonth <= 12) || endYear < 1800 || endYear > currentYear) {
				throw UserInfoErrorCode.INVALID_CAREER_END_DATE.toBaseException();
			}
		}
	}

	public void validateUserProjectDate(UserProjectDto userProjectDto) {
		int currentYear = LocalDate.now().getYear();
		int startYear = userProjectDto.startYear();
		int startMonth = userProjectDto.startMonth();
		Integer endYear = userProjectDto.endYear();
		Integer endMonth = userProjectDto.endMonth();

		if (!(1 <= startMonth && startMonth <= 12) || startYear < 1800 || startYear > currentYear) {
			throw UserInfoErrorCode.INVALID_PROJECT_START_DATE.toBaseException();
		}

		if (endYear != null && endMonth != null) {
			if (!(1 <= endMonth && endMonth <= 12) || endYear < 1800 || endYear > currentYear) {
				throw UserInfoErrorCode.INVALID_PROJECT_END_DATE.toBaseException();
			}
		}
	}
}
