package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;

public class UserRoleAdminOrPresidentValidator extends AbstractValidator {

    private final Role requestUserRole;

    private UserRoleAdminOrPresidentValidator(Role requestUserRole) {
        this.requestUserRole = requestUserRole;
    }

    public static UserRoleAdminOrPresidentValidator of(Role requestUserRole) {
        return new UserRoleAdminOrPresidentValidator(requestUserRole);
    }

    @Override
    public void validate() {
        if (this.requestUserRole != Role.ADMIN && this.requestUserRole != Role.PRESIDENT) {
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ALLOWED,
                    "You don't have auth"
            );
        }

        if (this.hasNext()) {
            this.next.validate();
        }
    }
}
