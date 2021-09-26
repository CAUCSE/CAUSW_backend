package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;

import java.util.List;

public class UserRoleValidator extends AbstractValidator {

    private final Role requestUserRole;

    private final List<Role> targetRoleList;

    private UserRoleValidator(Role requestUserRole, List<Role> targetRoleList) {
        this.requestUserRole = requestUserRole;
        this.targetRoleList = targetRoleList;
    }

    public static UserRoleValidator of(Role requestUserRole, List<Role> targetRoleList) {
        return new UserRoleValidator(requestUserRole, targetRoleList);
    }

    @Override
    public void validate() {
        for (Role targetRole : this.targetRoleList) {
            if (this.requestUserRole.equals(targetRole) || this.requestUserRole.equals(Role.ADMIN)) {
                return;
            }
        }

        throw new UnauthorizedException(
                ErrorCode.API_NOT_ALLOWED,
                "You don't have auth"
        );
    }
}
