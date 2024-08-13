package net.causw.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.stereotype.Component;

@Component
public class StudentIdIsNullValidator implements ConstraintValidator<UserValid, User> {

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        if (user.getStudentId() == null) {
            throw new BadRequestException(
                    ErrorCode.INVALID_STUDENT_ID,
                    "학번이 입력되지 않았습니다."
            );
        }
        return true;
    }
}
