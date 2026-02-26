package net.causw.app.main.domain.user.academic.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.account.enums.user.GraduationType;

public record GraduationDetailsResponse(
	String logId,
	Integer graduationYear,
	GraduationType graduationType,
	LocalDateTime changedAt) {
}
