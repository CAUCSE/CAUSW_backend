package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;

import java.time.LocalDateTime;

public class LockerExtendAccessValidator extends AbstractValidator {

    private final Boolean flag;

    private LockerExtendAccessValidator(Boolean flag) {
        this.flag = flag;
    }

    public static LockerExtendAccessValidator of(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(start) && now.isBefore(end)) {
            return new LockerExtendAccessValidator(true);
        }
        return new LockerExtendAccessValidator(false);
    }

    @Override
    public void validate() {
        if (!this.flag) {
            throw new BadRequestException(
                    ErrorCode.FLAG_NOT_AVAILABLE,
                    MessageUtil.LOCKER_EXTEND_NOT_ALLOWED
            );
        }
    }

}
