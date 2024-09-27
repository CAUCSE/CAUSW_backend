package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;

import java.util.Set;

public class UserRoleIsNoneValidator extends AbstractValidator {
    private final Set<Role> requestUserRoles;

    private UserRoleIsNoneValidator(Set<Role> requestUserRoles) {
        this.requestUserRoles = requestUserRoles;
    }

    public static UserRoleIsNoneValidator of(Set<Role> requestUserRoles) {
        return new UserRoleIsNoneValidator(requestUserRoles);
    }

    @Override
    public void validate() {
        if (this.requestUserRoles.contains(Role.NONE)) {
            throw new UnauthorizedException(
                    ErrorCode.NEED_SIGN_IN,
                    "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요."
            );
        }
    }
}
