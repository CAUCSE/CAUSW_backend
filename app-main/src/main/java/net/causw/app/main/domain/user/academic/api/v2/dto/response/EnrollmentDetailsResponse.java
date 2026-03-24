package net.causw.app.main.domain.user.academic.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;

public record EnrollmentDetailsResponse(
	String applicationId,
	AcademicRecordRequestStatus requestStatus,
	LocalDateTime requestedAt) {
}
