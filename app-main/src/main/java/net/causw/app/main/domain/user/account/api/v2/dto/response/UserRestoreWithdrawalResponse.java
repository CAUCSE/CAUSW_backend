package net.causw.app.main.domain.user.account.api.v2.dto.response;

public record UserRestoreWithdrawalResponse(
	String userId,
	boolean restored) {
}
