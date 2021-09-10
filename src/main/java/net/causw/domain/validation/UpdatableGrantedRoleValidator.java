package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;

public class UpdatableGrantedRoleValidator extends AbstractValidator {

    private final Role grantorRole;

    private final Role grantedRole;

    private UpdatableGrantedRoleValidator(Role grantorRole, Role grantedRole) {
        this.grantorRole = grantorRole;
        this.grantedRole = grantedRole;
    }

    public static UpdatableGrantedRoleValidator of(Role grantorRole, Role grantedRole) {
        return new UpdatableGrantedRoleValidator(grantorRole, grantedRole);
    }

    @Override
    public void validate() {
        /* When role of grantor is Admin, validate granted role
         * Granted role should not be Admin
         */
        if (this.grantorRole == Role.ADMIN && this.grantedRole != Role.ADMIN) {
            return;
        }
        /* When role of grantor is President, validate granted role
         * Granted role should not be Admin
         */
        if (this.grantorRole == Role.PRESIDENT && this.grantedRole != Role.ADMIN) {
            return;
        }
        /* When role of grantor is Leader_Circle, validate granted role
         * Granted role should be Leader_Circle
         */
        if (this.grantorRole == Role.LEADER_CIRCLE && this.grantedRole == Role.LEADER_CIRCLE) {
            return;
        }
        /* When role of grantor is Leader_Alumni, validate granted role
         * Granted role should be Leader_Alumni
         */
        if (this.grantorRole == Role.LEADER_ALUMNI && this.grantedRole == Role.LEADER_ALUMNI) {
            return;
        }

        throw new UnauthorizedException(
                ErrorCode.GRANT_ROLE_NOT_ALLOWED,
                "Grant role not allowed"
        );
    }
}
