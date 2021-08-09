package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;

public class UpdatableGranteeRoleValidator extends AbstractValidator {

    private final Role grantorRole;

    private final Role granteeRole;

    private UpdatableGranteeRoleValidator(Role grantorRole, Role granteeRole) {
        this.grantorRole = grantorRole;
        this.granteeRole = granteeRole;
    }

    public static UpdatableGranteeRoleValidator of(Role grantorRole, Role granteeRole) {
        return new UpdatableGranteeRoleValidator(grantorRole, granteeRole);
    }

    @Override
    public void validate() {
        /* When role of grantor is Leader_Circle, validate role of grantee
         * Role of grantee should be Common or None
         */
        if (this.grantorRole == Role.LEADER_CIRCLE &&
                (this.granteeRole == Role.COMMON || this.granteeRole == Role.NONE)) {
            this.pass();
            return;
        }
        /* When role of grantor is Leader_Alumni, validate role of grantee
         * Role of grantee should be Common or None
         */
        if (this.grantorRole == Role.LEADER_ALUMNI &&
                (this.granteeRole == Role.COMMON || this.granteeRole == Role.NONE)) {
            this.pass();
            return;
        }
        /* When role of grantor is Admin, validate role of grantee
         * Role of grantee should not be Admin
         */
        if (this.grantorRole == Role.ADMIN && this.granteeRole != Role.ADMIN) {
            this.pass();
            return;
        }
        /* When role of grantor is President, validate role of grantee
         * Role of grantee should not be Admin and President
         */
        if (this.grantorRole == Role.PRESIDENT &&
                this.granteeRole != Role.ADMIN && this.granteeRole != Role.PRESIDENT) {
            this.pass();
            return;
        }

        throw new UnauthorizedException(
                ErrorCode.API_NOT_ACCESSIBLE,
                "You don't have access."
        );
    }

    public void pass() {
        if (this.hasNext()) {
            this.next.validate();
        }
    }
}
