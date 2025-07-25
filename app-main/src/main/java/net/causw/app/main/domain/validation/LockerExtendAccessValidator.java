package net.causw.app.main.domain.validation;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.constant.MessageUtil;

public class LockerExtendAccessValidator extends AbstractValidator {
    private final Boolean flag;

    private LockerExtendAccessValidator(Boolean flag) {
        this.flag = flag;
    }

    public static LockerExtendAccessValidator of(Boolean flag) {
        return new LockerExtendAccessValidator(flag);
    }

    @Override
    public void validate() {
        if (!flag) {
            throw new BadRequestException(
                    ErrorCode.FLAG_NOT_AVAILABLE,
                    MessageUtil.LOCKER_EXTEND_NOT_ALLOWED);
        }
    }
}
