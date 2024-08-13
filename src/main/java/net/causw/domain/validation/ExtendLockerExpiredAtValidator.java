package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.time.LocalDateTime;

public class ExtendLockerExpiredAtValidator {
    public void validate(LocalDateTime src, LocalDateTime dst) {
        if (src.isEqual(dst)) {
            throw new BadRequestException(
                    ErrorCode.INVALID_EXPIRE_DATE,
                    "아직 반납기한을 확장할 수 없습니다."
            );
        }
    }
}
