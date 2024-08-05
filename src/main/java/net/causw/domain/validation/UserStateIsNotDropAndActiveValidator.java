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
public class UserStateIsNotDropAndActiveValidator implements ConstraintValidator<UserValid, User> {

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        UserState userState = user.getState();
        if (userState == UserState.DROP) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "추방된 사용자 입니다."
            );
        } else if (userState == UserState.ACTIVE) {
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ALLOWED,
                    "이미 등록된 사용자 입니다."
            );
        }
        return true;
    }
}