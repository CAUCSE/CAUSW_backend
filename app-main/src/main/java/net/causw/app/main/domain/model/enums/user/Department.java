package net.causw.app.main.domain.model.enums.user;

import java.util.Arrays;

import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Department {

	CS_DEPT("전자계산학과", 1972),
	DEPT_OF_CSE("컴퓨터공학과", 1993),
	SCHOOL_OF_CSE("컴퓨터공학부", 2003),
	SW_SCHOOL("소프트웨어학부", 2018),
	DEPT_OF_AI("AI학과", 2021);

	private final String name;
	private final Integer startYear;

	public static Department of(String name) {
		return Arrays.stream(Department.values())
			.filter(dept -> dept.name.equals(name))
			.findFirst()
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.INVALID_REQUEST_DEPARTMENT,
				String.format("name '%s' is invalid : not supported", name)
			));
	}

	public static Department fromAdmissionYear(Integer admissionYear) {
		if (admissionYear >= DEPT_OF_AI.startYear) { // 2021년도 입학부터 소프트웨어학부, AI학과 선택 가능
			throw new BadRequestException(
				ErrorCode.INVALID_REQUEST_DEPARTMENT,
				MessageUtil.DEPARTMENT_EXPLICITLY_REQUIRED
			);
		}

		return Arrays.stream(Department.values())
			.filter(dept -> admissionYear >= dept.startYear)
			.findFirst()
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.INVALID_REQUEST_DEPARTMENT,
				MessageUtil.INVALID_ADMISSION_YEAR
			));
	}

	public static Department fromAdmissionYearOrRequest(Integer admissionYear, Department request) {
		if (admissionYear < DEPT_OF_AI.getStartYear()) {
			return fromAdmissionYear(admissionYear);
		}

		if (request == null) {
			throw new BadRequestException(
				ErrorCode.INVALID_REQUEST_DEPARTMENT,
				MessageUtil.DEPARTMENT_EXPLICITLY_REQUIRED
			);
		}
		return request;
	}
}
