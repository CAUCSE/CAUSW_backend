package net.causw.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserRoleIsNoneValidator implements ConstraintValidator<UserValid, User> {

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        if (user.getRoles().contains(Role.NONE)) {
            throw new UnauthorizedException(
                    ErrorCode.NEED_SIGN_IN,
                    "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요."
            );
        }
        return true;
    }
}
