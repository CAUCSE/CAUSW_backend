package net.causw.app.main.domain.validation;

import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.global.constant.MessageUtil;

import java.util.Set;

import static net.causw.app.main.domain.policy.RolePolicy.*;


public class DelegatableRoleValidator extends AbstractValidator {

    private final Set<Role> delegatorRoles;

    private final Role delegatedRole;

    private final Set<Role> delegateeRoles;

    private DelegatableRoleValidator(Set<Role> delegatorRoles, Role delegatedRole, Set<Role> delegateeRoles) {
        this.delegatorRoles = delegatorRoles;
        this.delegatedRole = delegatedRole;
        this.delegateeRoles = delegateeRoles;
    }

    public static DelegatableRoleValidator of(Set<Role> delegatorRoles, Role delegatedRole, Set<Role> delegateeRoles) {
        return new DelegatableRoleValidator(delegatorRoles, delegatedRole, delegateeRoles);
    }

    @Override
    public void validate() {
        if (canDelegate()
                && canAssign(delegatedRole, delegateeRoles) && !isPrivilegeInverted(delegatorRoles, delegateeRoles)) {
            return;
        }

        throw customUnauthorizedException();
    }

    private boolean canDelegate() {
        // 위임할 권한이 위임 가능 대상이어야 하고 위임자가 해당 권한이어야 함.
        return getDelegatableRoles().contains(delegatedRole) && delegatorRoles.contains(delegatedRole);
    }

    private UnauthorizedException customUnauthorizedException() {
        return new UnauthorizedException(
                ErrorCode.ASSIGN_ROLE_NOT_ALLOWED,
                MessageUtil.DELEGATE_ROLE_NOT_ALLOWED
        );
    }
}
