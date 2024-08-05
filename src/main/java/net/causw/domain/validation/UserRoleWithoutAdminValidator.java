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
public class UserRoleWithoutAdminValidator implements ConstraintValidator<UserValid, User> {

    private final Set<Role> targetRoleSet = Set.of(Role.COMMON, Role.PROFESSOR);

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        for (Role targetRole : this.targetRoleSet) {
            if (user.getRoles().contains(targetRole)) {
                return true;
            }
        }

        throw new UnauthorizedException(
                ErrorCode.API_NOT_ALLOWED,
                "접근 권한이 없습니다."
        );
    }
}
