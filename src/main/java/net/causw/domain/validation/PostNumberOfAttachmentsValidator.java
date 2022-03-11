package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.List;

import static net.causw.domain.model.StaticValue.ATTACHMENT_LIST_LIMIT_SIZE;

public class PostNumberOfAttachmentsValidator extends AbstractValidator {
    private final List<String> attachmentList;

    private PostNumberOfAttachmentsValidator(List<String> attachmentList) {
        this.attachmentList = attachmentList;
    }

    public static PostNumberOfAttachmentsValidator of(List<String> attachmentList) {
        return new PostNumberOfAttachmentsValidator(attachmentList);
    }

    @Override
    public void validate() {
        if (attachmentList.size() > ATTACHMENT_LIST_LIMIT_SIZE) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "4개 이상의 파일을 첨부할 수 없습니다."
            );
        }
    }
}
