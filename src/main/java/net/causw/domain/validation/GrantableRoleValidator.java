package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;

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
         */
        if (this.grantorRole == Role.ADMIN)
            if (this.grantedRole != Role.ADMIN && this.granteeRole != Role.ADMIN
                    && (this.grantedRole != this.granteeRole))
                return;
        /* When role of grantor is President
         * Granted role should not be Admin, and Grantee role should not be Admin and President
         * Granted and grantee role should be different
         */
        if (this.grantorRole == Role.PRESIDENT)
            if (this.grantedRole != Role.ADMIN &&
                    (this.granteeRole != Role.ADMIN && this.granteeRole != Role.PRESIDENT)
                    && (this.grantedRole != this.granteeRole))
                return;
        /* When role of grantor is Leader_Circle
         * Granted role should be Leader_Circle, and Grantee role should be Common
         */
        if (this.grantorRole == Role.LEADER_CIRCLE)
            if (this.grantedRole == Role.LEADER_CIRCLE && this.granteeRole == Role.COMMON)
                return;
        /* When role of grantor is Leader_Alumni
         * Granted role should be Leader_Alumni, and Grantee role should be Common
         */
        if (this.grantorRole == Role.LEADER_ALUMNI)
            if (this.grantedRole == Role.LEADER_ALUMNI && this.granteeRole == Role.COMMON)
                return;

        throw new UnauthorizedException(
                ErrorCode.GRANT_ROLE_NOT_ALLOWED,
                String.format("Grant this role not allowed - Grantor Role : %s, Granted Role : %s, Grantee Role : %s",
                        this.grantorRole.getValue(),
                        this.grantedRole.getValue(),
                        this.granteeRole.getValue())
        );
    }
}
