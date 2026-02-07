package net.causw.app.main.domain.user.academic.service.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

public record AcademicRecordApplicationSummaryResult(
	String applicationId,
	String userId,
	String userName,
	String studentId,
	Department department,
	AcademicStatus currentAcademicStatus,
	AcademicStatus targetAcademicStatus,
	AcademicRecordRequestStatus requestStatus,
	LocalDateTime createdAt) {
	public static AcademicRecordApplicationSummaryResult from(UserAcademicRecordApplication application) {
		return new AcademicRecordApplicationSummaryResult(
			application.getId(),
			application.getUser().getId(),
			application.getUser().getName(),
			application.getUser().getStudentId(),
			application.getUser().getDepartment(),
			application.getUser().getAcademicStatus(),
			application.getTargetAcademicStatus(),
			application.getAcademicRecordRequestStatus(),
			application.getCreatedAt());
	}
}
