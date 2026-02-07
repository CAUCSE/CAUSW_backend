package net.causw.app.main.domain.user.academic.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

public record AcademicRecordApplicationDetailResult(
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
	LocalDateTime updatedAt) {
	public static AcademicRecordApplicationDetailResult from(UserAcademicRecordApplication application) {
		List<String> imageUrls = application.getUserAcademicRecordAttachImageList()
			.stream()
			.map(attachImage -> attachImage.getUuidFile().getFileUrl())
			.toList();

		return new AcademicRecordApplicationDetailResult(
			application.getId(),
			application.getUser().getId(),
			application.getUser().getName(),
			application.getUser().getStudentId(),
			application.getUser().getDepartment(),
			application.getUser().getAcademicStatus(),
			application.getTargetAcademicStatus(),
			application.getTargetCompletedSemester(),
			application.getAcademicRecordRequestStatus(),
			application.getNote(),
			application.getRejectMessage(),
			imageUrls,
			application.getCreatedAt(),
			application.getUpdatedAt());
	}
}
