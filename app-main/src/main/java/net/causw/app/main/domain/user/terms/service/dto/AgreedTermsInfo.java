package net.causw.app.main.domain.user.terms.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.terms.entity.TermsType;

public record AgreedTermsInfo(
	TermsType type,
	int version,
	LocalDateTime agreedAt) {
}
