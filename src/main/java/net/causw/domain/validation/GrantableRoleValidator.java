package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.policy.domain.RolePolicy;

import java.util.Arrays;
import java.util.Set;

public class GrantableRoleValidator extends AbstractValidator {

    private final Set<Role> grantorRoles;

    private final Set<Role> delegatorRoles;

    private final Role grantedRole;

    private final Set<Role> granteeRoles;

    private GrantableRoleValidator(Set<Role> grantorRoles, Set<Role> delegatorRoles, Role grantedRole, Set<Role> granteeRoles) {
        this.grantorRoles = grantorRoles;
        this.delegatorRoles = delegatorRoles;
        this.grantedRole = grantedRole;
        this.granteeRoles = granteeRoles;
    }

    public static GrantableRoleValidator of(Set<Role> grantorRoles, Set<Role> delegatorRoles, Role grantedRole, Set<Role> granteeRoles) {
        return new GrantableRoleValidator(grantorRoles, delegatorRoles, grantedRole, granteeRoles);
    }

    @Override
    public void validate() {
        // 부여 불가 권한이 아니어야하고 위임자가 있을 시 조건(isNotDelegatableByDelegator)을 충족해야함.
        if (RolePolicy.NON_GRANTABLE_ROLES.contains(this.grantedRole) && isNotProxyDelegatableByDelegator()) {
            throw  customUnauthorizedException();
        }

        // 일반 권한 부여의 경우 수혜자의 권한 상관 없이 부여 가능함.
        if (grantedRole.equals(Role.COMMON)) {
            if (hasAnyRole(grantorRoles, RolePolicy.DEFAULT_GRANTOR_ROLES.toArray(new Role[0])))
                return;
        }

        // 학생회장 부여의 경우 부학생회장과 학생회 권한이 같이 삭제되므로 수혜자가 일반 권한 또는 이 두 권한일 경우 부여 가능함.
        else if (grantedRole.equals(Role.PRESIDENT)) {
            if (hasAnyRole(grantorRoles, RolePolicy.getGrantorRoles(Role.PRESIDENT).toArray(new Role[0]))
                    && hasAnyRole(granteeRoles, RolePolicy.ROLES_DELEGATABLE_BY_PRESIDENT.toArray(new Role[0]))) {
                return;
            }
        }

        // 수혜자가 특수 권한이 아닌 일반 권한일 경우에만 위임 가능함.
        else {
            if (hasAnyRole(grantorRoles, RolePolicy.DEFAULT_GRANTOR_ROLES.toArray(new Role[0]))
                    && hasAnyRole(granteeRoles, Role.COMMON)) {
                return;
            }
        }

        throw  customUnauthorizedException();
    }

    private boolean isNotProxyDelegatableByDelegator() {
        // 위임지가 있을 시 위임자가 해당 권한이어야하고 위임 불가 권한이 아니어야함.
        return delegatorRoles != null &&
                (!delegatorRoles.contains(grantedRole) || RolePolicy.NON_PROXY_DELEGATABLE_ROLES.contains(grantedRole));
    }

    private boolean hasAnyRole(Set<Role> targetRole, Role... targetedRoles) {
        return Arrays.stream(targetedRoles).anyMatch(targetRole::contains);
    }

    private UnauthorizedException customUnauthorizedException() {
        return new UnauthorizedException(
                ErrorCode.GRANT_ROLE_NOT_ALLOWED,
                MessageUtil.GRANT_ROLE_NOT_ALLOWED
        );
    }
}
