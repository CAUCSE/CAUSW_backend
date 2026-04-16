package net.causw.app.main.domain.user.account.service.dto.request;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.DeletedUserSortType;
import net.causw.app.main.domain.user.account.enums.user.Department;

public record DeletedUserQueryCondition(
	String keyword,
	Department department,
	Integer admissionYearFrom,
	Integer admissionYearTo,
	AcademicStatus academicStatus,
	DeletedUserSortType sortBy) {
}
