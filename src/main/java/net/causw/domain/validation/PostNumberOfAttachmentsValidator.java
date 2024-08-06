package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.List;

import static net.causw.domain.model.util.StaticValue.MAX_NUM_FILE_ATTACHMENTS;

public class PostNumberOfAttachmentsValidator {

    public void isValid(List<String> attachmentList) {
        if (attachmentList.size() > MAX_NUM_FILE_ATTACHMENTS) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "4개 이상의 파일을 첨부할 수 없습니다."
            );
        }
    }
}
