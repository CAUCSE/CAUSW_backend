package net.causw.app.main.domain.community.ceremony.service.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;
import net.causw.app.main.domain.community.ceremony.enums.RelationType;

public record CeremonyCreateCommand(
	CeremonyType ceremonyType,
	CeremonyCategory ceremonyCategory,
	String ceremonyCustomCategory,
	LocalDate startDate,
	LocalDate endDate,
	LocalTime startTime,
	LocalTime endTime,
	RelationType relationType,
	String familyRelation,
	String alumniRelation,
	String alumniName,
	String alumniAdmissionYear,
	String content,
	String address,
	String postalAddress,
	String detailedAddress,
	String contact,
	String link,
	Boolean isSetAll,
	List<String> targetAdmissionYears) {
}
