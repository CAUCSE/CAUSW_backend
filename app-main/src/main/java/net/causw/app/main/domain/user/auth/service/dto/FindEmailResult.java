package net.causw.app.main.domain.user.auth.service.dto;

import java.time.LocalDate;
import java.util.List;

public record FindEmailResult(
	String email,
	LocalDate createdAt,
	List<FindEmailSocialAccountResult> socialAccounts) {
	public static FindEmailResult of(String email, LocalDate createdAt, List<FindEmailSocialAccountResult> socialAccounts) {
		return new FindEmailResult(email, createdAt, socialAccounts);
	}
}
