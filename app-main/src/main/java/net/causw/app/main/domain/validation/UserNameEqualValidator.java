package net.causw.app.main.domain.validation;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class UserNameEqualValidator extends AbstractValidator {

    private final String srcUserName;

    private final String targetUserName;

    private UserNameEqualValidator(String srcUserName, String targetUserName) {
        this.srcUserName = srcUserName;
        this.targetUserName = targetUserName;
    }

    public static UserNameEqualValidator of(String srcUserName, String targetUserName) {
        return new UserNameEqualValidator(srcUserName, targetUserName);
    }

    @Override
    public void validate() {
        if (!this.srcUserName.equals(this.targetUserName)) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "답할 답장의 작성자가 아닙니다."
            );
        }
    }
}
