package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.UserState;

public class UserStateIsNotDropAndActiveValidator extends AbstractValidator {

    private final UserState userState;

    private UserStateIsNotDropAndActiveValidator(UserState userState) {
        this.userState = userState;
    }

    public static UserStateIsNotDropAndActiveValidator of(UserState userState) {
        return new UserStateIsNotDropAndActiveValidator(userState);
    }

    @Override
    public void validate() {
        if (this.userState == UserState.DROP) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "추방된 사용자 입니다."
            );
        } else if (this.userState == UserState.ACTIVE) {
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ALLOWED,
                    "이미 등록된 사용자 입니다."
            );
        }
    }
}