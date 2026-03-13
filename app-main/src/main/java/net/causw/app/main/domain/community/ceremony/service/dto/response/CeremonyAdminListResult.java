package net.causw.app.main.domain.community.ceremony.service.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;

public record CeremonyAdminListResult(
	String id,
	String applicantName,
	String applicantStudentId,
	CeremonyState state,
	LocalDate startDate,
	LocalDateTime createdAt,
	String category) {
}
