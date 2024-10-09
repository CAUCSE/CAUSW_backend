package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.UserState;

public class UserStateIsDropOrIsInActiveValidator extends AbstractValidator {
    private final UserState userState;

    private UserStateIsDropOrIsInActiveValidator(UserState userState) {
        this.userState = userState;
    }

    public static UserStateIsDropOrIsInActiveValidator of(UserState userState) {
        return new UserStateIsDropOrIsInActiveValidator(userState);
    }

    @Override
    public void validate() {
        if (!(this.userState.equals(UserState.REJECT) || this.userState.equals(UserState.DROP) || this.userState.equals(UserState.INACTIVE) || this.userState.equals(UserState.DELETED))) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "등록된 사용자가 아닙니다."
            );
        }
    }
}
