package net.causw.domain.validation;

import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.policy.domain.RolePolicy;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (delegator == null ? !canGrant() : !canProxyDelegate()) {
            throw  customUnauthorizedException();
        }

        // 일반 권한 부여의 경우 수혜자의 권한 상관 없이 부여 가능함.
        if (grantedRole.equals(Role.COMMON)) {
            return;
        }

        // 학생회장 부여의 경우 부학생회장과 학생회 권한이 같이 삭제되므로 수혜자가 일반 권한 외에 두 권한이어도 부여 가능함.
        else if (grantedRole.equals(Role.PRESIDENT)) {
            if (hasAnyRole(grantee.getRoles(), RolePolicy.ROLES_UPDATABLE_BY_PRESIDENT)) {
                return;
            }
        }

        // 동문회장 부여의 경우 수혜자가 일반 권한이고 졸업생일 경우에만 부여 가능함
        else if (grantedRole.equals(Role.LEADER_ALUMNI)) {
            if (hasAnyRole(grantee.getRoles(), Role.COMMON)
                    && grantee.getAcademicStatus().equals(AcademicStatus.GRADUATED))
                return;
        }

        // 그 외의 경우 수혜자가 일반 권한일 경우에만 부여 가능함.
        else {
            if (hasAnyRole(grantee.getRoles(), Role.COMMON)) {
                return;
            }
        }

        throw  customUnauthorizedException();
    }

    public boolean canGrant() {
        Set<Role> totalGrantableRoles = grantorRoles.stream()
                .flatMap(role -> RolePolicy.GRANTABLE_ROLES.getOrDefault(role, Set.of()).stream())
                .collect(Collectors.toSet());

        // 부여자는 부여할 권한 및 수혜자의 모든 권한에 대한 부여 가능 권한을 가지고 있어야 함.
        return totalGrantableRoles.contains(grantedRole) && totalGrantableRoles.containsAll(grantee.getRoles());
    }

    public boolean canProxyDelegate() {
        // 위임자가 대리 위임할 권한이어야 함.
        if (!delegator.getRoles().contains(grantedRole)) {
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
