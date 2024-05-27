package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.time.LocalDateTime;

public class ExtendLockerExpiredAtValidator extends AbstractValidator {
    private final LocalDateTime src;
    private final LocalDateTime dst;

    private ExtendLockerExpiredAtValidator(
            LocalDateTime src,
            LocalDateTime dst
    ) {
        this.src = src;
        this.dst = dst;
    }

    public static ExtendLockerExpiredAtValidator of(
            LocalDateTime src,
            LocalDateTime dst
    ) {
        return new ExtendLockerExpiredAtValidator(src, dst);
    }

    @Override
    public void validate() {
        if (src.isEqual(dst)) {
            throw new BadRequestException(
                    ErrorCode.INVALID_EXPIRE_DATE,
                    "아직 반납기한을 확장할 수 없습니다."
            );
        }
    }
}
