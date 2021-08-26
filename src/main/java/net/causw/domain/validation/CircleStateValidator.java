package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class CircleStateValidator extends AbstractValidator {

    private final boolean isDeleted;

    private CircleStateValidator(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public static CircleStateValidator of(boolean isDeleted) {
        return new CircleStateValidator(isDeleted);
    }

    @Override
    public void validate() {
        if (this.isDeleted) {
            throw new BadRequestException(
                    ErrorCode.TARGET_DELETED,
                    "The circle is deleted"
            );
        }

        if (this.hasNext()) {
            this.next.validate();
        }
    }
}
