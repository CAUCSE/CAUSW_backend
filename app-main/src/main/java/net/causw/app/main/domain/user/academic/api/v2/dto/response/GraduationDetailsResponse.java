package net.causw.app.main.domain.user.academic.api.v2.dto.response;

import java.time.LocalDateTime;

public record GraduationDetailsResponse(
	String logId,
	Integer graduationYear,
	LocalDateTime changedAt) {
}
