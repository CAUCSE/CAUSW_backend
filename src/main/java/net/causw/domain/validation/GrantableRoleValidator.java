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
        if (!canGrant() && !canProxyDelegate()) {
            throw  customUnauthorizedException();
        }

        // 일반 권한 부여의 경우 수혜자의 권한 상관 없이 부여 가능함.
        if (grantedRole.equals(Role.COMMON)) {
            return;
        }

        // 학생회장 부여의 경우 부학생회장과 학생회 권한이 같이 삭제되므로 수혜자가 일반 권한 외에 두 권한이어도 부여 가능함.
        else if (grantedRole.equals(Role.PRESIDENT)) {
            if (hasAnyRole(granteeRoles, RolePolicy.ROLES_UPDATABLE_BY_PRESIDENT)) {
                return;
            }
        }

        // 그 외의 경우 수혜자가 특수 권한이 아닌 일반 권한일 경우에만 부여 가능함.
        else {
            if (hasAnyRole(granteeRoles, Role.COMMON)) {
                return;
            }
        }

        throw  customUnauthorizedException();
    }

    public boolean canGrant() {
        // 부여자가 부여할 권한에 대한 부여 가능 권한을 가지고 있어야 함.
        return grantorRoles.stream().anyMatch(role -> RolePolicy.GRANTABLE_ROLES
                .getOrDefault(role, Set.of()).contains(grantedRole));
    }

    public boolean canProxyDelegate() {
        // 위임자가 있을 경우 위임자가 대리 위임할 권한이어야 함.
        if (delegatorRoles == null || !delegatorRoles.contains(grantedRole)) {
            return false;
        }

        // 부여자가 대리 위임할 권한에 대한 대리 위임 가능 권한을 가지고 있어야 함.
        return grantorRoles.stream().anyMatch(role -> RolePolicy.PROXY_DELEGATABLE_ROLES
                .getOrDefault(role, Set.of()).contains(grantedRole));
    }

    private boolean hasAnyRole(Set<Role> targetRole, Set<Role> targetedRoles) {
        return targetedRoles.stream().anyMatch(targetRole::contains);
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
