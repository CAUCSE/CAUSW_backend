package net.causw.app.main.domain.validation;

import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;
import net.causw.app.main.domain.model.enums.user.UserState;

public class UserStateIsDeletedValidator extends AbstractValidator {

    private final UserState userState;

    private UserStateIsDeletedValidator(UserState userState) {
        this.userState = userState;
    }

    public static UserStateIsDeletedValidator of(UserState userState) {
        return new UserStateIsDeletedValidator(userState);
    }

    @Override
    public void validate() {
        if (this.userState.equals(UserState.DELETED)) {
            throw new UnauthorizedException(
                    ErrorCode.DELETED_USER,
                    "삭제된 사용자 입니다."
            );
        }
    }
}
