package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class LockerInUseValidator {
    public void validate(boolean isInUse) {
        if (isInUse) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "사용 중인 사물함입니다."
            );
        }
    }
}
