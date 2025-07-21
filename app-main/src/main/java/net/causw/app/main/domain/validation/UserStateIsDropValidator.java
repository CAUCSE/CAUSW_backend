package net.causw.app.main.domain.validation;

import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;
import net.causw.app.main.domain.model.enums.user.UserState;

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
