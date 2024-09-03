package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.UserState;

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
