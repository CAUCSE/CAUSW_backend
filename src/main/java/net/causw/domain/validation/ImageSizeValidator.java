package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import static net.causw.domain.model.util.StaticValue.IMAGE_LIMIT_SIZE;

public class ImageSizeValidator extends AbstractValidator {
    private final MultipartFile image;

    private ImageSizeValidator(MultipartFile image) {
        this.image = image;
    }

    public static ImageSizeValidator of(MultipartFile image) {
        return new ImageSizeValidator(image);
    }

    @Override
    public void validate() {
        if (image.getSize() > IMAGE_LIMIT_SIZE) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "20MB 이상의 이미지를 첨부할 수 없습니다."
            );
        }
    }
}
