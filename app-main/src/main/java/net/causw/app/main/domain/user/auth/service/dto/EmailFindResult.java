package net.causw.app.main.domain.user.auth.service.dto;

import java.time.LocalDate;
import java.util.List;

public record EmailFindResult(
	String email,
	LocalDate createdAt,
	List<SocialAccountSummary> socialAccounts) {
	public static EmailFindResult of(String email, LocalDate createdAt, List<SocialAccountSummary> socialAccounts) {
		return new EmailFindResult(email, createdAt, socialAccounts);
	}

	public record SocialAccountSummary(
		String provider,
		LocalDate createdAt) {
		public static SocialAccountSummary of(String provider, LocalDate createdAt) {
			return new SocialAccountSummary(provider, createdAt);
		}
	}
}
