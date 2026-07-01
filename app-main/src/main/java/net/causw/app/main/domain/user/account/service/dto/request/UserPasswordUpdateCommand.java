package net.causw.app.main.domain.user.account.service.dto.request;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserPasswordUpdateRequest;

public record UserPasswordUpdateCommand(
	String email,
	String currentPassword,
	String newPassword,
	String newPasswordConfirm) {

	public static UserPasswordUpdateCommand from(UserPasswordUpdateRequest request) {
		return new UserPasswordUpdateCommand(
			request.email(),
			request.currentPassword(),
			request.newPassword(),
			request.newPasswordConfirm());
	}
}
