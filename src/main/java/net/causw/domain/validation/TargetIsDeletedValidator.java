package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class TargetIsDeletedValidator {
    public void validate(boolean isDeleted, String domain) {
        if (isDeleted) {
            throw new BadRequestException(
                    ErrorCode.TARGET_DELETED,
                    String.format("삭제된 %s 입니다.", domain)
            );
        }
    }
}
