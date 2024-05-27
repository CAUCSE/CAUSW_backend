package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

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
                    "사물함 신청 기간이 아닙니다. 공지를 확인해주세요."
            );
        }
    }
}
