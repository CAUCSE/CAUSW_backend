package net.causw.app.main.domain.validation;

import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;
import net.causw.app.main.domain.model.enums.user.Role;

import java.util.EnumSet;
import java.util.Set;

public class UserRoleValidator extends AbstractValidator {

    private final Set<Role> requestUserRoles;

    private final Set<Role> targetRoleSet;

    private UserRoleValidator(Set<Role> requestUserRoles, Set<Role> targetRoleSet) {
        this.requestUserRoles = requestUserRoles;
        this.targetRoleSet = targetRoleSet;
    }

    public static UserRoleValidator of(Set<Role> requestUserRoles, Set<Role> targetRoleSet) {
        return new UserRoleValidator(requestUserRoles, targetRoleSet);
    }

    @Override
    public void validate() {

        if (this.requestUserRoles.stream().anyMatch(role -> EnumSet.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT).contains(role))) {
            return;
        }

        if (this.requestUserRoles.stream().anyMatch(this.targetRoleSet::contains)) {
            return;
        }

        throw new UnauthorizedException(
                ErrorCode.API_NOT_ALLOWED,
                "접근 권한이 없습니다. 사용자 역할: " + this.requestUserRoles + ", 허용된 역할: " + this.targetRoleSet
        );
    }
}
