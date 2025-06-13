package net.causw.domain.validation;

import lombok.Getter;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.util.MessageUtil;

import java.util.Arrays;
import java.util.Set;


public class GrantableRoleValidator extends AbstractValidator {

    @Getter
    private static final Set<Role> grantableRoles = Set.of(Role.ADMIN, Role.PRESIDENT, Role.LEADER_ALUMNI);

    private final Set<Role> grantorRoles;

    private final Role grantedRole;

    private final Set<Role> granteeRoles;

    private GrantableRoleValidator(Set<Role> grantorRoles, Role grantedRole, Set<Role> granteeRoles) {
        this.grantorRoles = grantorRoles;
        this.grantedRole = grantedRole;
        this.granteeRoles = granteeRoles;
    }

    public static GrantableRoleValidator of(Set<Role> grantorRoles, Role grantedRole, Set<Role> granteeRoles) {
        return new GrantableRoleValidator(grantorRoles, grantedRole, granteeRoles);
    }

    @Override
    public void validate() {
        // 위임할 권한이 위임 가능 대상이어야 하고 위임자가 해당 권한이어야 함.
        if (grantableRoles.contains(this.grantedRole) && this.grantorRoles.contains(this.grantedRole)) {
            // 피위임자가 특수 권한이 아닌 일반 권한일 경우에만 위임 가능함.
            // 단, 학생회장 위임의 경우 부학생회장과 학생회 권한이 같이 삭제되므로 이 두 권한을 포함해 가능함.
            if (this.grantedRole.equals(Role.PRESIDENT)
                    && hasAnyRole(this.granteeRoles, Role.VICE_PRESIDENT, Role.COUNCIL, Role.COMMON)) {
                return;
            }
            else if (hasAnyRole(this.granteeRoles, Role.COMMON)) {
                return;
            }
        }

        throw new UnauthorizedException(
                ErrorCode.GRANT_ROLE_NOT_ALLOWED,
                MessageUtil.GRANT_ROLE_NOT_ALLOWED
        );
    }

    private boolean hasAnyRole(Set<Role> targetRole, Role... targetedRoles) {
        return Arrays.stream(targetedRoles).anyMatch(targetRole::contains);
    }

}
