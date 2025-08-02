package net.causw.app.main.domain.validation;

import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.global.constant.MessageUtil;

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
        if (this.userState == UserState.DROP) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "추방된 사용자 입니다."
            );
        }

        if (this.userState == UserState.INACTIVE) {
            throw new UnauthorizedException(
                    ErrorCode.INACTIVE_USER,
                    "비활성화된 사용자 입니다."
            );
        }

        if (this.userState == UserState.DELETED) {
            throw new UnauthorizedException(
                    ErrorCode.DELETED_USER,
                    "삭제된 사용자 입니다."
            );
        }

        if (this.userState == UserState.SUSPENDED) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    MessageUtil.REPORT_USER_SUSPENDED_LOGIN
            );
        }

    }
}
