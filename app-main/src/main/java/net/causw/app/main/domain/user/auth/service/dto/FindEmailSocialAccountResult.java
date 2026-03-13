package net.causw.app.main.domain.user.auth.service.dto;

import java.time.LocalDate;

public record FindEmailSocialAccountResult(
	String provider,
	LocalDate createdAt) {
	public static FindEmailSocialAccountResult of(String provider, LocalDate createdAt) {
		return new FindEmailSocialAccountResult(provider, createdAt);
	}
}
