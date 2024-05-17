package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.Role;

import java.util.List;

public class UserRoleWithoutAdminValidator extends AbstractValidator {

    private final Role requestUserRole;

    private final List<Role> targetRoleList;

    private UserRoleWithoutAdminValidator(Role requestUserRole, List<Role> targetRoleList) {
        this.requestUserRole = requestUserRole;
        this.targetRoleList = targetRoleList;
    }

    public static UserRoleWithoutAdminValidator of(Role requestUserRole, List<Role> targetRoleList) {
        return new UserRoleWithoutAdminValidator(requestUserRole, targetRoleList);
    }

    @Override
    public void validate() {
        for (Role targetRole : this.targetRoleList) {
            if (this.requestUserRole.equals(targetRole)) {
                return;
            }
        }

        throw new UnauthorizedException(
                ErrorCode.API_NOT_ALLOWED,
                "접근 권한이 없습니다."
        );
    }
}
