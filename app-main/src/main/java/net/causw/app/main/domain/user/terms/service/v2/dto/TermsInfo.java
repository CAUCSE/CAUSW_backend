package net.causw.app.main.domain.user.terms.service.v2.dto;

import java.time.LocalDate;

import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.domain.user.terms.entity.TermsType;

public record TermsInfo(
	String title,
	TermsType type,
	boolean isRequired,
	int version,
	LocalDate effectiveDate,
	String content) {

	public static TermsInfo from(Terms terms) {
		return new TermsInfo(
			terms.getTitle(),
			terms.getType(),
			terms.isRequired(),
			terms.getVersion(),
			terms.getEffectiveDate(),
			terms.getContent());
	}
}
