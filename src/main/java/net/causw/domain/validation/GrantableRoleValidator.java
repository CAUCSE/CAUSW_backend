package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.Role;

public class GrantableRoleValidator extends AbstractValidator {

    private final Role grantorRole;

    private final Role grantedRole;

    private final Role granteeRole;

    private GrantableRoleValidator(Role grantorRole, Role grantedRole, Role granteeRole) {
        this.grantorRole = grantorRole;
        this.grantedRole = grantedRole;
        this.granteeRole = granteeRole;
    }

    public static GrantableRoleValidator of(Role grantorRole, Role grantedRole, Role granteeRole) {
        return new GrantableRoleValidator(grantorRole, grantedRole, granteeRole);
    }

    @Override
    public void validate() {
        /* When role of grantor is Admin
         * Granted and grantee role should not be Admin
         * Granted and grantee role should be different
         * Grantee role should not be Leader Circle or Leader Alumni
         *   => They will automatically granted while other granting process since the roles has unique user
         */
        if (this.grantorRole.equals(Role.ADMIN)) {
            if (this.granteeRole != Role.ADMIN) {
                if(this.grantedRole.equals(Role.LEADER_CIRCLE) || this.granteeRole.equals(Role.LEADER_CIRCLE)){
                    return;
                } else if(this.granteeRole.equals(Role.COMMON)){
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
        else if (this.grantorRole.equals(Role.PRESIDENT)) {
            if (this.grantedRole != Role.ADMIN
                    && (this.granteeRole != Role.ADMIN && this.granteeRole != Role.PRESIDENT)) {
                if(this.grantedRole.equals(Role.LEADER_CIRCLE) || this.granteeRole.equals(Role.LEADER_CIRCLE)){
                    return;
                } else if(this.granteeRole.equals(Role.COMMON)){
                    return;
                } else if(this.grantedRole.equals(Role.COMMON)){
                    return;
                }
            }
        }
        /* When role of grantor is Leader_Circle
         * Granted role should be Leader_Circle, and Grantee role should be Common
         */
        else if (this.grantorRole.getValue().contains("LEADER_CIRCLE")) {
            if(this.grantedRole.equals(Role.LEADER_CIRCLE)){
                if(this.granteeRole != Role.ADMIN && this.granteeRole != Role.PRESIDENT && this.granteeRole !=Role.VICE_PRESIDENT
                        && this.granteeRole != Role.LEADER_ALUMNI && this.granteeRole != Role.PROFESSOR ){
                    return;
                }
            }
        }
        /* When role of grantor is Leader_Alumni
         * Granted role should be Leader_Alumni, and Grantee role should be Common
         */
        else if (this.grantorRole.equals(Role.LEADER_ALUMNI)) {
            if (this.grantedRole == Role.LEADER_ALUMNI && this.granteeRole == Role.COMMON) {
                return;
            }
        }

        throw new UnauthorizedException(
                ErrorCode.GRANT_ROLE_NOT_ALLOWED,
                String.format("권한을 부여할 수 없습니다.")
        );
    }
}
