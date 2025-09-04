package net.causw.app.main.domain.validation;

import java.util.EnumSet;
import java.util.Set;

import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

public class UserRoleValidator extends AbstractValidator {

	private final Set<Role> requestUserRoles;

	private final Set<Role> targetRoleSet;

	private UserRoleValidator(Set<Role> requestUserRoles, Set<Role> targetRoleSet) {
		this.requestUserRoles = requestUserRoles;
		this.targetRoleSet = targetRoleSet;
	}

	public static UserRoleValidator of(Set<Role> requestUserRoles, Set<Role> targetRoleSet) {
		return new UserRoleValidator(requestUserRoles, targetRoleSet);
	}

	@Override
	public void validate() {

		if (this.requestUserRoles.stream()
			.anyMatch(role -> EnumSet.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT).contains(role))) {
			return;
		}

		if (this.requestUserRoles.stream().anyMatch(this.targetRoleSet::contains)) {
			return;
		}

		throw new UnauthorizedException(
			ErrorCode.API_NOT_ALLOWED,
			MessageUtil.API_NOT_ACCESSIBLE
		);
	}
}
