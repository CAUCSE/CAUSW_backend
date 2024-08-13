package net.causw.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.stereotype.Component;

@Setter
@Component
public class UserNotEqualValidator implements ConstraintValidator<UserValid, User> {

    private String targetUserId;

    public void validate(String srcUserId, String targetUserId) {
        if (srcUserId.equals(targetUserId)) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "해당 사용자는 명령을 수행할 수 없습니다."
            );
        }
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        validate(user.getId(), targetUserId);
        return true;
    }
}
