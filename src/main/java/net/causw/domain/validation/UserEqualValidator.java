package net.causw.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.stereotype.Component;

@Setter
@Component
public class UserEqualValidator implements ConstraintValidator<UserValid, User> {

    //TODO AOP proxy 사용할 것
    private String targetUserId;

    public void validate(String srcUserId, String targetUserId) {
        if (srcUserId.equals(targetUserId)) {
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ALLOWED,
                    "접근 권한이 없습니다."
            );
        }
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        if (user.getId().equals(targetUserId)) {
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ALLOWED,
                    "접근 권한이 없습니다."
            );
        }
        return true;
    }
}
