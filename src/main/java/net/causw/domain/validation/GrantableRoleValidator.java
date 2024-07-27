package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.Role;

import java.util.Set;


public class GrantableRoleValidator extends AbstractValidator {

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
        /* When role of grantor is Admin
         * Granted and grantee role should not be Admin
         * Granted and grantee role should be different
         * Grantee role should not be Leader Circle or Leader Alumni
         *   => They will automatically granted while other granting process since the roles has unique user
         */
        if (this.grantorRoles.contains(Role.ADMIN)) {
            if (!this.granteeRoles.contains(Role.ADMIN)) {
                if(this.grantedRole.equals(Role.LEADER_CIRCLE) || this.granteeRoles.contains(Role.LEADER_CIRCLE)){
                    return;
                } else if(this.granteeRoles.contains(Role.COMMON)){
                    return;
                } else if(this.grantedRole.equals(Role.COMMON)){
                    return;
                }
            }
        }
        /* When role of grantor is President
         * Granted role should not be Admin, and Grantee role should not be Admin and President
         * Granted and grantee role should be different
         * Grantee role should not be Leader Circle or Leader Alumni
         *   => They will automatically granted while other granting process since the roles has unique user
         */
        else if (this.grantorRoles.contains(Role.PRESIDENT)) {
            if (this.grantedRole != Role.ADMIN
                    && (!this.granteeRoles.contains(Role.ADMIN) && !this.granteeRoles.contains(Role.PRESIDENT))) {
                if(this.grantedRole.equals(Role.LEADER_CIRCLE) || this.granteeRoles.contains(Role.LEADER_CIRCLE)){
                    return;
                } else if(this.granteeRoles.contains(Role.COMMON)){
                    return;
                } else if(this.grantedRole.equals(Role.COMMON)){
                    return;
                }
            }
        }
        /* When role of grantor is Leader_Circle
         * Granted role should be Leader_Circle, and Grantee role should be Common
         */
        else if (this.grantorRoles.contains(Role.LEADER_CIRCLE)) {
            if(this.grantedRole.equals(Role.LEADER_CIRCLE)){
                if(!this.granteeRoles.contains(Role.ADMIN) && !this.granteeRoles.contains(Role.PRESIDENT) && !this.granteeRoles.contains(Role.VICE_PRESIDENT)
                        && !this.granteeRoles.contains(Role.LEADER_ALUMNI) && !this.granteeRoles.contains(Role.PROFESSOR)){
                    return;
                }
            }
        }
        /* When role of grantor is Leader_Alumni
         * Granted role should be Leader_Alumni, and Grantee role should be Common
         */
        else if (this.grantorRoles.contains(Role.LEADER_ALUMNI)) {
            if (this.grantedRole == Role.LEADER_ALUMNI && this.granteeRoles.contains(Role.COMMON)) {
                return;
            }
        }

        throw new UnauthorizedException(
                ErrorCode.GRANT_ROLE_NOT_ALLOWED,
                String.format("권한을 부여할 수 없습니다.")
        );
    }
}
