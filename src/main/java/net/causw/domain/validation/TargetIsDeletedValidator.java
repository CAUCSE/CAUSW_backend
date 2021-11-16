package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;

import java.util.Locale;

public class TargetIsDeletedValidator extends AbstractValidator {

    private final boolean isDeleted;

    private final String target;

    private TargetIsDeletedValidator(boolean isDeleted, String target) {
        this.isDeleted = isDeleted;
        this.target = target;
    }

    public static TargetIsDeletedValidator of(boolean isDeleted, String target) {
        return new TargetIsDeletedValidator(isDeleted, target);
    }

    @Override
    public void validate() {
        if (this.isDeleted) {
            throw new BadRequestException(
                    ErrorCode.TARGET_DELETED,
                    String.format("삭제된 %s 입니다.", this.target)
            );
        }
    }
}
