package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class UserNotEqualValidator extends AbstractValidator {

    private final String srcUserId;

    private final String targetUserId;

    private UserNotEqualValidator(String srcUserId, String targetUserId) {
        this.srcUserId = srcUserId;
        this.targetUserId = targetUserId;
    }

    public static UserNotEqualValidator of(String srcUserId, String targetUserId) {
        return new UserNotEqualValidator(srcUserId, targetUserId);
    }

    @Override
    public void validate() {
        if (this.srcUserId.equals(this.targetUserId)) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "API cannot be performed"
            );
        }
    }
}
