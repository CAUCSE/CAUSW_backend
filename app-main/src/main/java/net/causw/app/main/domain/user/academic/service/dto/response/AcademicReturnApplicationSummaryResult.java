package net.causw.app.main.domain.user.academic.service.dto.response;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import java.time.LocalDateTime;

public record AcademicReturnApplicationSummaryResult(
		String applicationId,
		String userId,
		String userName,
		String studentId,
		Department department,
		AcademicStatus currentAcademicStatus,
		AcademicStatus targetAcademicStatus,
		AcademicRecordRequestStatus requestStatus,
		LocalDateTime createdAt
) {
	public static AcademicReturnApplicationSummaryResult from(UserAcademicRecordApplication application) {
		return new AcademicReturnApplicationSummaryResult(
				application.getId(),
				application.getUser().getId(),
				application.getUser().getName(),
				application.getUser().getStudentId(),
				application.getUser().getDepartment(),
				application.getUser().getAcademicStatus(),
				application.getTargetAcademicStatus(),
				application.getAcademicRecordRequestStatus(),
				application.getCreatedAt()
		);
	}
}
