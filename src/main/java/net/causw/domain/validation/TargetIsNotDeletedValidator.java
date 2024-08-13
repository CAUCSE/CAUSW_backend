package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class TargetIsNotDeletedValidator {
    public void validate(boolean isDeleted, String domain) {
        if (!isDeleted) {
            throw new BadRequestException(
                    ErrorCode.TARGET_DELETED,
                    String.format("삭제되지 않은 %s 입니다.", domain)
            );
        }
    }
}
