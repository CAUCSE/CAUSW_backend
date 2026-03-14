package net.causw.app.main.domain.user.account.service.dto.response;

import java.util.List;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record UserRestoreResult(
	String id,
	UserState state,
	List<String> roles) {

	public static UserRestoreResult from(User user) {
		return new UserRestoreResult(
			user.getId(),
			user.getState(),
			user.getRoles().stream().map(Enum::name).toList());
	}
}
