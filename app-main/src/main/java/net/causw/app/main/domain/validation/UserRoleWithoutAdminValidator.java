package net.causw.app.main.domain.validation;

import java.util.Set;

import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

public class UserRoleWithoutAdminValidator extends AbstractValidator {

	private final Set<Role> requestUserRoles;

	private final Set<Role> targetRoleSet;

	private UserRoleWithoutAdminValidator(Set<Role> requestUserRoles, Set<Role> targetRoleSet) {
		this.requestUserRoles = requestUserRoles;
		this.targetRoleSet = targetRoleSet;
	}

	public static UserRoleWithoutAdminValidator of(Set<Role> requestUserRoles, Set<Role> targetRoleSet) {
		return new UserRoleWithoutAdminValidator(requestUserRoles, targetRoleSet);
	}

	@Override
	public void validate() {
		for (Role targetRole : this.targetRoleSet) {
			if (this.requestUserRoles.contains(targetRole)) {
				return;
			}
		}

		throw new UnauthorizedException(
			ErrorCode.API_NOT_ALLOWED,
			"접근 권한이 없습니다."
		);
	}
}
