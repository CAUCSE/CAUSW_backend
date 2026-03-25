package net.causw.app.main.domain.community.ceremony.service.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;

public record CeremonyDetailResult(
	String id,
	String title,
	String type,
	String category,
	LocalDate startDate,
	LocalDate endDate,
	LocalTime startTime,
	LocalTime endTime,
	String applicant,
	String subject,
	String content,
	List<String> attachedImageUrlList,
	String address,
	String postalAddress,
	String detailedAddress,
	String contact,
	String link,
	Boolean isSetAll,
	List<String> targetAdmissionYears,
	CeremonyState state,
	String note) {
}