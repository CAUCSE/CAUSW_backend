package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.UserDomainModel;

public class PasswordCorrectValidator extends AbstractValidator {

    private final UserDomainModel userDomainModel;

    private final String password;

    private PasswordCorrectValidator(UserDomainModel userDomainModel, String password) {
        this.userDomainModel = userDomainModel;
        this.password = password;
    }

    public static PasswordCorrectValidator of(UserDomainModel userDomainModel, String password) {
        return new PasswordCorrectValidator(userDomainModel, password);
    }

    @Override
    public void validate() {
        if (!this.userDomainModel.getPassword().equals(this.password)) {
            throw new UnauthorizedException(
                    ErrorCode.INVALID_SIGNIN,
                    "Invalid sign in data"
            );
        }
    }
}
