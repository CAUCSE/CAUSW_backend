package net.causw.app.main.domain.validation;

import static net.causw.app.main.domain.policy.RolePolicy.*;

import java.util.Set;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

public class GrantableRoleValidator extends AbstractValidator {

	private final Set<Role> grantorRoles;

	private final User delegator;

	private final Role grantedRole;

	private final User grantee;

	private GrantableRoleValidator(Set<Role> grantorRoles, User delegator, Role grantedRole, User grantee) {
		this.grantorRoles = grantorRoles;
		this.delegator = delegator;
		this.grantedRole = grantedRole;
		this.grantee = grantee;
	}

	public static GrantableRoleValidator of(Set<Role> grantorRoles, User delegator, Role grantedRole, User grantee) {
		return new GrantableRoleValidator(grantorRoles, delegator, grantedRole, grantee);
	}

	@Override
	public void validate() {
		if ((delegator == null ? !canGrant() : !canProxyDelegate())
			|| !canAssign(grantedRole, grantee.getRoles())
			|| isPrivilegeInverted(grantorRoles, grantee.getRoles())) {
			throw customUnauthorizedException();
		}

		// 동문회장 부여의 경우 수혜자가 졸업생일 경우에만 부여 가능함
		if (grantedRole.equals(Role.LEADER_ALUMNI)) {
			if (grantee.getAcademicStatus().equals(AcademicStatus.GRADUATED))
				return;
		} else {
			return;
		}

		throw customUnauthorizedException();
	}

	private boolean canGrant() {
		// 부여자는 부여할 권한에 대한 부여 가능 권한을 가지고 있어야 함.
		return grantorRoles.stream().anyMatch(role -> getGrantableRoles(role).contains(grantedRole));
	}

	private boolean canProxyDelegate() {
		// 위임자가 대리 위임할 권한이어야 함.
		if (!delegator.getRoles().contains(grantedRole)) {
			return false;
		}

		// 부여자가 대리 위임할 권한에 대한 대리 위임 가능 권한을 가지고 있어야 함.
		return grantorRoles.stream().anyMatch(role -> getProxyDelegatableRoles(role).contains(grantedRole));
	}

	private UnauthorizedException customUnauthorizedException() {
		return new UnauthorizedException(
			ErrorCode.ASSIGN_ROLE_NOT_ALLOWED,
			MessageUtil.GRANT_ROLE_NOT_ALLOWED
		);
	}
}
