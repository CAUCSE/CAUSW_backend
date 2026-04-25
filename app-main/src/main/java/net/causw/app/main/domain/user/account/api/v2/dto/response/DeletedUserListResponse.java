package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record DeletedUserListResponse(

	String id,
	String name,
	String email,
	String studentId,
	Integer admissionYear,
	Department department,
	UserState userState,
	AcademicStatus academicStatus,
	LocalDateTime deletedAt,
	String dropReason) {
}
