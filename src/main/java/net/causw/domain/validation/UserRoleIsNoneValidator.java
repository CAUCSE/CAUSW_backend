package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;

public class UserRoleIsNoneValidator extends AbstractValidator {
    private final Role requestUserRole;

    private UserRoleIsNoneValidator(Role requestUserRole) {
        this.requestUserRole = requestUserRole;
    }

    public static UserRoleIsNoneValidator of(Role requestUserRole) {
        return new UserRoleIsNoneValidator(requestUserRole);
    }

    @Override
    public void validate() {
        if (this.requestUserRole.equals(Role.NONE)) {
            throw new UnauthorizedException(
                    ErrorCode.NEED_SIGN_IN,
                    "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요."
            );
        }
    }
}
