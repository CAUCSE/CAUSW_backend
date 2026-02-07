package net.causw.app.main.domain.user.academic.api.v2.dto.response;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import java.time.LocalDateTime;
import java.util.List;

public record AcademicReturnApplicationDetailResponse(
		String applicationId,
		String userId,
		String userName,
		String studentId,
		Department department,
		AcademicStatus currentAcademicStatus,
		AcademicStatus targetAcademicStatus,
		Integer targetCompletedSemester,
		AcademicRecordRequestStatus requestStatus,
		String note,
		String rejectMessage,
		List<String> attachImageUrls,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
