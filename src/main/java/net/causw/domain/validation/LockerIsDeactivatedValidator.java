package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class LockerIsDeactivatedValidator  {
    public void validate(boolean isActive) {
        if (!isActive) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "사물함이 사용 불가능한 상태입니다."
            );
        }
    }
}
