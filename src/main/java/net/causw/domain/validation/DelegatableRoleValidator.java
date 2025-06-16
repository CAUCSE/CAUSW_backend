package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.policy.domain.RolePolicy;

import java.util.Arrays;
import java.util.Set;


public class DelegatableRoleValidator extends AbstractValidator {

    private final Set<Role> delegatorRoles;

    private final Role delegatedRole;

    private final Set<Role> delegateeRoles;

    private DelegatableRoleValidator(Set<Role> delegatorRoles, Role delegatedRole, Set<Role> delegateeRoles) {
        this.delegatorRoles = delegatorRoles;
        this.delegatedRole = delegatedRole;
        this.delegateeRoles = delegateeRoles;
    }

    public static DelegatableRoleValidator of(Set<Role> grantorRoles, Role grantedRole, Set<Role> granteeRoles) {
        return new DelegatableRoleValidator(grantorRoles, grantedRole, granteeRoles);
    }

    @Override
    public void validate() {
        // 위임할 권한이 위임 가능 대상이어야 하고 위임자가 해당 권한이어야 함.
        if (!RolePolicy.DELEGATABLE_ROLES.contains(this.delegatedRole) || !this.delegatorRoles.contains(this.delegatedRole)) {
            throw  customUnauthorizedException();
        }
        
        // 학생회장 위임의 경우 부학생회장과 학생회 권한이 같이 삭제되므로 피위임자가 일반 권한 외에 두 권한이어도 위임 가능함.
        if (this.delegatedRole.equals(Role.PRESIDENT)) {
            if (hasAnyRole(this.delegateeRoles, RolePolicy.ROLES_UPDATABLE_BY_PRESIDENT))
                return;
        }

        // 그 외의 경우 피위임자가 특수 권한이 아닌 일반 권한일 경우에만 위임 가능함.
        else {
            if (hasAnyRole(this.delegateeRoles, Role.COMMON))
                return;
        }

        throw customUnauthorizedException();
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
