package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static net.causw.domain.model.util.StaticValue.MAX_NUM_FILE_ATTACHMENTS;

public class PostNumberOfAttachmentsValidator extends AbstractValidator {
    private final List<MultipartFile> multipartFileList;

    private PostNumberOfAttachmentsValidator(List<MultipartFile> multipartFileList) {
        this.multipartFileList = multipartFileList;
    }

    public static PostNumberOfAttachmentsValidator of(List<MultipartFile> multipartFileList) {
        return new PostNumberOfAttachmentsValidator(multipartFileList);
    }

    @Override
    public void validate() {
        if (multipartFileList.size() > MAX_NUM_FILE_ATTACHMENTS) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "4개 이상의 파일을 첨부할 수 없습니다."
            );
        }
    }
}
