package net.causw.app.main.domain.community.ceremony.service.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;

public record CeremonySummaryResult(
	String id,
	String title,
	String type,
	String category,
	LocalDate startDate,
	LocalDate endDate,
	LocalTime startTime,
	LocalTime endTime,
	CeremonyState state) {
}