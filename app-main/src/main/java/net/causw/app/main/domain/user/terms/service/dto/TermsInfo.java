package net.causw.app.main.domain.user.terms.service.dto;

import java.time.LocalDate;

import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.domain.user.terms.entity.TermsType;

public record TermsInfo(
	String id,
	String title,
	TermsType type,
	boolean isRequired,
	int version,
	LocalDate effectiveDate,
	String content) {

	public static TermsInfo from(Terms terms) {
		return new TermsInfo(
			terms.getId(),
			terms.getTitle(),
			terms.getType(),
			terms.isRequired(),
			terms.getVersion(),
			terms.getEffectiveDate(),
			terms.getContent());
	}
}
