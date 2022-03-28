package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import static net.causw.domain.model.StaticValue.IMAGE_EXTENSION_LIST;

public class ImageExtensionValidator extends AbstractValidator {
    private final MultipartFile image;

    private ImageExtensionValidator(MultipartFile image) {
        this.image = image;
    }

    public static ImageExtensionValidator of(MultipartFile image) {
        return new ImageExtensionValidator(image);
    }

    @Override
    public void validate() {
        String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());

        if (extension == null || !IMAGE_EXTENSION_LIST.contains(extension.toUpperCase())) {
            throw new BadRequestException(
                    ErrorCode.INVALID_FILE_EXTENSION,
                    "해당 파일은 이미지가 아닙니다."
            );
        }
    }
}
