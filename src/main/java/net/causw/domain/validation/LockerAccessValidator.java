package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class LockerAccessValidator {
    public void validate(Boolean flag) {
        if (!flag) {
            throw new BadRequestException(
                    ErrorCode.FLAG_NOT_AVAILABLE,
                    "사물함 신청 기간이 아닙니다. 공지를 확인해주세요."
            );
        }
    }
}
