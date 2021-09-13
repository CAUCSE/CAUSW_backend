package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class TargetIsDeletedValidator extends AbstractValidator {

    private final boolean isDeleted;

    private TargetIsDeletedValidator(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public static TargetIsDeletedValidator of(boolean isDeleted) {
        return new TargetIsDeletedValidator(isDeleted);
    }

    @Override
    public void validate() {
        if (this.isDeleted) {
            throw new BadRequestException(
                    ErrorCode.TARGET_DELETED,
                    "The target is deleted"
            );
        }
    }
}
