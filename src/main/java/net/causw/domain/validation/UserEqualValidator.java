package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;

public class UserEqualValidator extends AbstractValidator {

    private final String srcUserId;

    private final String targetUserId;

    private UserEqualValidator(String srcUserId, String targetUserId) {
        this.srcUserId = srcUserId;
        this.targetUserId = targetUserId;
    }

    public static UserEqualValidator of(String srcUserId, String targetUserId) {
        return new UserEqualValidator(srcUserId, targetUserId);
    }

    @Override
    public void validate() {
        if (!this.srcUserId.equals(this.targetUserId)) {
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ALLOWED,
                    "접근 권한이 없습니다."
            );
        }
    }
}