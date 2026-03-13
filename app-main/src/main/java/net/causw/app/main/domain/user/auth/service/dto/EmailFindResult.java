package net.causw.app.main.domain.user.auth.service.dto;

import java.time.LocalDate;
import java.util.List;

public record EmailFindResult(
	String email,
	LocalDate createdAt,
	List<SocialAccountSummaryResult> socialAccounts) {
	public static EmailFindResult of(String email, LocalDate createdAt, List<SocialAccountSummaryResult> socialAccounts) {
		return new EmailFindResult(email, createdAt, socialAccounts);
	}
}
