package net.causw.app.main.domain.user.academic.service.dto.result;

import lombok.Builder;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import java.time.LocalDateTime;

@Builder
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
		return AcademicReturnApplicationSummaryResult.builder()
				.applicationId(application.getId())
				.userId(application.getUser().getId())
				.userName(application.getUser().getName())
				.studentId(application.getUser().getStudentId())
				.department(application.getUser().getDepartment())
				.currentAcademicStatus(application.getUser().getAcademicStatus())
				.targetAcademicStatus(application.getTargetAcademicStatus())
				.requestStatus(application.getAcademicRecordRequestStatus())
				.createdAt(application.getCreatedAt())
				.build();
	}
}
