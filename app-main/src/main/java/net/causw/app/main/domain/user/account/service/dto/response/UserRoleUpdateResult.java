package net.causw.app.main.domain.user.account.service.dto.response;

import java.util.List;

import net.causw.app.main.domain.user.account.entity.user.User;

public record UserRoleUpdateResult(
	String id,
	List<String> roles) {

	public static UserRoleUpdateResult from(User user) {
		return new UserRoleUpdateResult(
			user.getId(),
			user.getRoles().stream().map(Enum::name).toList());
	}
}
