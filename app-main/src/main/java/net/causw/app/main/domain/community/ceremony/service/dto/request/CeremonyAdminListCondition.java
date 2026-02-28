package net.causw.app.main.domain.community.ceremony.service.dto.request;

import java.time.LocalDate;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;

public record CeremonyAdminListCondition(
	LocalDate fromDate,
	LocalDate toDate,
	CeremonyState state) {
}
