package net.causw.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.UserState;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.stereotype.Component;

@Component
public class UserStateIsDropOrIsInActiveValidator implements ConstraintValidator<UserValid, User> {

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        UserState userState = user.getState();
        if (!(userState.equals(UserState.DROP) || userState.equals(UserState.INACTIVE))) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "등록된 사용자가 아닙니다."
            );
        }
        return true;
    }
}
