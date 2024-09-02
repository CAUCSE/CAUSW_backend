package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

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
                    "사물함 연장 신청 기간이 아닙니다. 공지를 확인해주세요."
            );
        }
    }
}
