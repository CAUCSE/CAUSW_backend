package net.causw.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.ImageLocation;
import net.causw.domain.validation.valid.UtilValid;
import org.springframework.stereotype.Component;

@Component
public class ImageLocationTypeValidator implements ConstraintValidator<UtilValid, String> {

    @Override
    public boolean isValid(String type, ConstraintValidatorContext constraintValidatorContext) {
        if (ImageLocation.of(type) == null){
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "ImageLocation type이 일치하지 않습니다."
            );
        }
        return true;
    }
}
