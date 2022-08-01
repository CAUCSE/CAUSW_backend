package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.time.LocalDateTime;

public class LockerExpiredAtValidator extends AbstractValidator {
    private final LocalDateTime src;
    private final LocalDateTime dst;

    private LockerExpiredAtValidator(
            LocalDateTime src,
            LocalDateTime dst
    ) {
        this.src = src;
        this.dst = dst;
    }

    public static LockerExpiredAtValidator of(
            LocalDateTime src,
            LocalDateTime dst
    ) {
        return new LockerExpiredAtValidator(src, dst);
    }

    @Override
    public void validate() {
        if (src.isAfter(dst)) {
            throw new BadRequestException(
                    ErrorCode.INVALID_EXPIRE_DATE,
                    "잘못된 반납날 입니다."
            );
        }
    }
}
