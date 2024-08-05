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
public class UserStateValidator implements ConstraintValidator<UserValid, User> {

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        UserState state = user.getState();

        if (state == UserState.DROP) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "추방된 사용자 입니다."
            );
        }

        if (state == UserState.INACTIVE) {
            throw new UnauthorizedException(
                    ErrorCode.INACTIVE_USER,
                    "비활성화된 사용자 입니다."
            );
        }

        if (state == UserState.AWAIT) {
            throw new UnauthorizedException(
                    ErrorCode.AWAITING_USER,
                    "대기 중인 사용자 입니다."
            );
        }

        if (state == UserState.REJECT) {
            throw new UnauthorizedException(
                    ErrorCode.REJECT_USER,
                    "가입이 거절된 사용자 입니다."
            );
        }

        return true;
    }
}
