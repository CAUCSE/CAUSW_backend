package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.model.UserState;

public class UserStateValidator extends AbstractValidator {

    private final UserDomainModel userDomainModel;

    private UserStateValidator(UserDomainModel userDomainModel) {
        this.userDomainModel = userDomainModel;
    }

    public static UserStateValidator of(UserDomainModel userDomainModel) {
        return new UserStateValidator(userDomainModel);
    }

    @Override
    public void validate() {
        if (this.userDomainModel.getState() == UserState.BLOCKED) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "Blocked user"
            );
        }

        if (this.userDomainModel.getState() == UserState.INACTIVE) {
            throw new UnauthorizedException(
                    ErrorCode.INACTIVE_USER,
                    "Inactive user"
            );
        }

        if (this.hasNext()) {
            this.next.validate();
        };
    }
}
