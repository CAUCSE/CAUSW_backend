package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.time.LocalDateTime;

public record UserWithdrawResponse(
	LocalDateTime deletedAt,
	LocalDateTime recoverableUntil) {
	public static UserWithdrawResponse of(LocalDateTime deletedAt) {
		return new UserWithdrawResponse(deletedAt, deletedAt.plusDays(30));
	}
}
