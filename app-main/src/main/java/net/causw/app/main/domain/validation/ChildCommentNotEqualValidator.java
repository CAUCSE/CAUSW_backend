package net.causw.app.main.domain.validation;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class ChildCommentNotEqualValidator extends AbstractValidator {

    private final String srcChildCommentId;

    private final String targetChildCommentId;

    private ChildCommentNotEqualValidator(String srcChildCommentId, String targetChildCommentId) {
        this.srcChildCommentId = srcChildCommentId;
        this.targetChildCommentId = targetChildCommentId;
    }

    public static ChildCommentNotEqualValidator of(String srcChildCommentId, String targetChildCommentId) {
        return new ChildCommentNotEqualValidator(srcChildCommentId, targetChildCommentId);
    }

    @Override
    public void validate() {
        if (this.srcChildCommentId.equals(this.targetChildCommentId)) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "해당 답장에 답할 수 없습니다."
            );
        }
    }
}
