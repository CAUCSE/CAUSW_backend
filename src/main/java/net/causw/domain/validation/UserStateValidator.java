package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.UserState;

public class UserStateValidator extends AbstractValidator {

    private final UserState userState;

    private UserStateValidator(UserState userState) {
        this.userState = userState;
    }

    public static UserStateValidator of(UserState userState) {
        return new UserStateValidator(userState);
    }

    @Override
    public void validate() {
        if (this.userState == UserState.BLOCKED) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "Blocked user"
            );
        }

        if (this.userState == UserState.INACTIVE) {
            throw new UnauthorizedException(
                    ErrorCode.INACTIVE_USER,
                    "Inactive user"
            );
        }

        if (this.userState == UserState.WAIT) {
            throw new UnauthorizedException(
                    ErrorCode.AWAITING_USER,
                    "Awaiting user"
            );
        }

        if (this.hasNext()) {
            this.next.validate();
        }
    }
}