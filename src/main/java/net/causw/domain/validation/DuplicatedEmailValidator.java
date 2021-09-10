package net.causw.domain.validation;

import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class DuplicatedEmailValidator extends AbstractValidator {

    private final UserPort userPort;

    private final String email;

    private DuplicatedEmailValidator(UserPort userPort, String email) {
        this.userPort = userPort;
        this.email = email;
    }

    public static DuplicatedEmailValidator of(UserPort userPort, String email) {
        return new DuplicatedEmailValidator(userPort, email);
    }

    @Override
    public void validate() {
        if (this.userPort.findByEmail(this.email).isPresent()) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    "This email already exist"
            );
        }
    }
}
