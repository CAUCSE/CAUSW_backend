package net.causw.app.main.domain.validation;

import static net.causw.global.constant.StaticValue.*;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

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
		if (multipartFileList != null && multipartFileList.size() > MAX_NUM_FILE_ATTACHMENTS) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				"4개 이상의 파일을 첨부할 수 없습니다."
			);
		}
	}
}
