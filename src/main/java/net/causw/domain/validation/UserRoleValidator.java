package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.Role;

import java.util.EnumSet;
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

        if (EnumSet.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT).contains(this.requestUserRole)) {
            return;
        }

        for (Role targetRole : this.targetRoleList) {
            if (this.requestUserRole.getValue().contains(targetRole.getValue())) {
                return;
            }
        }

        throw new UnauthorizedException(
                ErrorCode.API_NOT_ALLOWED,
                "접근 권한이 없습니다."
        );
    }
}
