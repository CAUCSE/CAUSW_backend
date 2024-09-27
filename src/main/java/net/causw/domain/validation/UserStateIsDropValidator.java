package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.UserState;

public class UserStateIsDropValidator extends AbstractValidator {
    private final UserState userState;

    private UserStateIsDropValidator(UserState userState) {
        this.userState = userState;
    }

    public static UserStateIsDropValidator of(UserState userState) {
        return new UserStateIsDropValidator(userState);
    }

    @Override
    public void validate() {
        if (this.userState != UserState.DROP) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "추방된 사용자가 아닙니다."
            );
        }
    }
}
