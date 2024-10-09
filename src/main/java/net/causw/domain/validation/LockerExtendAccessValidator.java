package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;

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
