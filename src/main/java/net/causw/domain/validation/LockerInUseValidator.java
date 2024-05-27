package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class LockerInUseValidator extends AbstractValidator {
    private final Boolean isInUse;

    private LockerInUseValidator(Boolean isInUse) {
        this.isInUse = isInUse;
    }

    public static LockerInUseValidator of(Boolean isInUse) {
        return new LockerInUseValidator(isInUse);
    }

    @Override
    public void validate() {
        if (isInUse) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "사용 중인 사물함입니다."
            );
        }
    }
}
