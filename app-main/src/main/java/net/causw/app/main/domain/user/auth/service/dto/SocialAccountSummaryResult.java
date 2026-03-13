package net.causw.app.main.domain.user.auth.service.dto;

import java.time.LocalDate;

public record SocialAccountSummaryResult(
	String provider,
	LocalDate createdAt) {
	public static SocialAccountSummaryResult of(String provider, LocalDate createdAt) {
		return new SocialAccountSummaryResult(provider, createdAt);
	}
}
