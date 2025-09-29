package net.causw.app.main.domain.resolver;

import java.util.List;

import net.causw.app.main.domain.model.enums.user.Department;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class DepartmentResolver {

	/**
	 * 학과/학부명 사용 기간
	 * - 입학년도 기준으로 학과/학부명을 결정할 수 있는 범위만 정의
	 * - AI 학과 개설 이후로는 입학년도 기준으로 학과/학부명 결정할 수 없음
	 */
	private static final List<DepartmentPeriod> departmentPeriods = List.of(
		new DepartmentPeriod(Department.SCHOOL_OF_SW, 2018, StaticValue.CAU_AI_START_YEAR - 1),
		new DepartmentPeriod(Department.SCHOOL_OF_CSE, 2003, 2017),
		new DepartmentPeriod(Department.DEPT_OF_CSE, 1993, 2002),
		new DepartmentPeriod(Department.DEPT_OF_CS, StaticValue.CAU_SW_START_YEAR, 1992)
	);

	public static Department resolveByAdmissionYearOrDepartmentName(Integer admissionYear, String departmentName) {
		// AI 학과 개설 이전 입학생은 입학년도로 학과/학부 결정
		if (admissionYear < StaticValue.CAU_AI_START_YEAR) {
			return resolveByAdmissionYear(admissionYear);
		}

		return Department.of(departmentName);
	}

	public static Department resolveByAdmissionYearOrDepartment(Integer admissionYear, Department request) {
		// AI 학과 개설 이전 입학생은 입학년도로 학과/학부 결정
		if (admissionYear < StaticValue.CAU_AI_START_YEAR) {
			return resolveByAdmissionYear(admissionYear);
		}

		// AI 학과 개설 이후 입학생은 학과/학부 선택 필수
		if (request == null) {
			throw new BadRequestException(
				ErrorCode.INVALID_REQUEST_DEPARTMENT,
				MessageUtil.DEPARTMENT_EXPLICITLY_REQUIRED
			);
		}
		return request;
	}

	public static Department resolveByAdmissionYear(int admissionYear) {
		return departmentPeriods.stream()
			.filter(period ->
				admissionYear >= period.startYear() && admissionYear <= period.endYear())
			.findFirst()
			.map(DepartmentPeriod::department)
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.INVALID_REQUEST_DEPARTMENT,
				MessageUtil.INVALID_ADMISSION_YEAR
			));
	}

	private record DepartmentPeriod(Department department, Integer startYear, Integer endYear) {
	}
}
