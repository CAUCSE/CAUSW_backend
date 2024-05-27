package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import static net.causw.domain.model.util.StaticValue.ATTACHMENT_LIMIT_SIZE;

public class AttachmentSizeValidator extends AbstractValidator {
    private final MultipartFile attachment;

    private AttachmentSizeValidator(MultipartFile attachment) {
        this.attachment = attachment;
    }

    public static AttachmentSizeValidator of(MultipartFile attachment) {
        return new AttachmentSizeValidator(attachment);
    }

    @Override
    public void validate() {
        if (attachment.getSize() > ATTACHMENT_LIMIT_SIZE) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "50MB 이상의 파일을 첨부할 수 없습니다."
            );
        }
    }
}
