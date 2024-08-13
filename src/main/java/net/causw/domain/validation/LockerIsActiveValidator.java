package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class LockerIsActiveValidator {
    public void validate(boolean isActive) {
        if (isActive) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "이미 사용 가능한 사물함입니다."
            );
        }
    }
}
