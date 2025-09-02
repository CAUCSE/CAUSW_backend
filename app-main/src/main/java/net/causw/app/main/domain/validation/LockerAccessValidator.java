package net.causw.app.main.domain.validation;

import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class LockerAccessValidator extends AbstractValidator {
    private final Boolean flag;

    private LockerAccessValidator(Boolean flag) {
        this.flag = flag;
    }

    public static LockerAccessValidator of(Boolean flag) {
        return new LockerAccessValidator(flag);
    }

    @Override
    public void validate() {
        if (!flag) {
            throw new BadRequestException(
                    ErrorCode.FLAG_NOT_AVAILABLE,
                    MessageUtil.LOCKER_REGISTER_NOT_ALLOWED
            );
        }
    }
}
