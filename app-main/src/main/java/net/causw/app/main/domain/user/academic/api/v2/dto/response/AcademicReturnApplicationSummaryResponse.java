package net.causw.app.main.domain.user.academic.api.v2.dto.response;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import java.time.LocalDateTime;

public record AcademicReturnApplicationSummaryResponse(
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
}
