package net.causw.domain.validation;

import lombok.RequiredArgsConstructor;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.ImageLocation;
import org.springframework.web.multipart.MultipartFile;

public class ImageLocationTypeValidator extends AbstractValidator{

    String type;
    private ImageLocationTypeValidator(String type) {
        this.type = type;
    }


    public static ImageLocationTypeValidator of(String type){
        return new ImageLocationTypeValidator(type);
    }
    @Override
    public void validate() {
        if (ImageLocation.of(type) == null){
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "ImageLocation type이 일치하지 않습니다."
            );
        }
    }
}
