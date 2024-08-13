package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.time.LocalDateTime;

public class LockerExpiredAtValidator {
    public void validate(LocalDateTime src, LocalDateTime dst) {
        if (src.isAfter(dst)) {
            throw new BadRequestException(
                    ErrorCode.INVALID_EXPIRE_DATE,
                    "잘못된 반납날 입니다."
            );
        }
    }
}
