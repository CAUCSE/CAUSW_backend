package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class TargetIsNotDeletedValidator extends AbstractValidator {

    private final boolean isDeleted;

    private final String domain;

    private TargetIsNotDeletedValidator(boolean isDeleted, String domain) {
        this.isDeleted = isDeleted;
        this.domain = domain;
    }

    public static TargetIsNotDeletedValidator of(boolean isDeleted, String domain) {
        return new TargetIsNotDeletedValidator(isDeleted, domain);
    }

    @Override
    public void validate() {
        if (!this.isDeleted) {
            throw new BadRequestException(
                    ErrorCode.TARGET_DELETED,
                    String.format("삭제되지 않은 %s 입니다.", this.domain)
            );
        }
    }
}
