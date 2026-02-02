package net.causw.app.main.domain.user.account.service.dto.request;

import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record UserQueryCondition(
	UserState userState,
	Role userRole,
	String keyword) {
}
