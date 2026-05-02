package net.causw.app.main.domain.user.account.service.dto.response;

import net.causw.app.main.domain.user.account.entity.user.User;

public record UserRestoreWithdrawalResult(
	String userId,
	boolean restored) {
	public static UserRestoreWithdrawalResult from(User user) {
		return new UserRestoreWithdrawalResult(
			user.getId(),
			true);
	}
}
