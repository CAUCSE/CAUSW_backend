package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class LockerIsDeactivatedValidator extends AbstractValidator {
    private final Boolean isActive;

    private LockerIsDeactivatedValidator(Boolean isActive) {
        this.isActive = isActive;
    }

    public static LockerIsDeactivatedValidator of(Boolean isActive) {
        return new LockerIsDeactivatedValidator(isActive);
    }

    @Override
    public void validate() {
        if (!this.isActive) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "사물함이 사용 불가능한 상태입니다."
            );
        }
    }
}
