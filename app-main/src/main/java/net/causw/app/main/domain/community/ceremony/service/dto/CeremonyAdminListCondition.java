package net.causw.app.main.domain.community.ceremony.service.dto;

import java.time.LocalDate;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;

public record CeremonyAdminListCondition(
	LocalDate fromDate,
	LocalDate toDate,
	CeremonyState state) {
}
