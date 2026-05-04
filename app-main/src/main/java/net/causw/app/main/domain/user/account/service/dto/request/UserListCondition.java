package net.causw.app.main.domain.user.account.service.dto.request;

import java.util.List;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserSortType;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record UserListCondition(
	String keyword,
	List<UserState> states,
	AcademicStatus academicStatus,
	Department department,
	Integer admissionYearFrom,
	Integer admissionYearTo,
	UserSortType sortBy) {
}
