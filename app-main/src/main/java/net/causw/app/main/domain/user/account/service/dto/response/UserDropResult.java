package net.causw.app.main.domain.user.account.service.dto.response;

import java.util.List;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record UserDropResult(
	String id,
	UserState state,
	List<String> roles,
	String dropReason) {

	public static UserDropResult from(User user) {
		return new UserDropResult(
			user.getId(),
			user.getState(),
			user.getRoles().stream().map(Enum::name).toList(),
			user.getRejectionOrDropReason());
	}
}
