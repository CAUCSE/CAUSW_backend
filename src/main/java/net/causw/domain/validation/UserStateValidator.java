package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.UserState;

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

        if (this.userState == UserState.AWAIT) {
            throw new UnauthorizedException(
                    ErrorCode.AWAITING_USER,
                    "대기 중인 사용자 입니다."
            );
        }

        if (this.userState == UserState.REJECT) {
            throw new UnauthorizedException(
                    ErrorCode.REJECT_USER,
                    "가입이 거절된 사용자 입니다."
            );
        }
    }
}
