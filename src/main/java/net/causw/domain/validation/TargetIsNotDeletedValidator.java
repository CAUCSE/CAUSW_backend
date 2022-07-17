package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class TargetIsDeletedValidator extends AbstractValidator {

    private final boolean isDeleted;

    private final String domain;

    private TargetIsDeletedValidator(boolean isDeleted, String domain) {
        this.isDeleted = isDeleted;
        this.domain = domain;
    }

    public static TargetIsDeletedValidator of(boolean isDeleted, String domain) {
        return new TargetIsDeletedValidator(isDeleted, domain);
    }

    @Override
    public void validate() {
        if (this.isDeleted) {
            throw new BadRequestException(
                    ErrorCode.TARGET_DELETED,
                    String.format("삭제된 %s 입니다.", this.domain)
            );
        }
    }
}
