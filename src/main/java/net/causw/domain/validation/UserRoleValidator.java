package net.causw.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Setter
@Component
public class UserRoleValidator implements ConstraintValidator<UserValid, User> {

    private Set<Role> targetRoleSet;

    public void validate(Set<Role> requestUserRoles, Set<Role> targetRoleSet) {
        if (requestUserRoles.stream().anyMatch(role -> EnumSet.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT).contains(role))) {
            return;
        }
        if (requestUserRoles.stream().anyMatch(targetRoleSet::contains)) {
            return;
        }

        throw new UnauthorizedException(
                ErrorCode.API_NOT_ALLOWED,
                "접근 권한이 없습니다."
        );
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        Set<Role> requestUserRoles = user.getRoles();
        if (requestUserRoles.stream().anyMatch(role -> EnumSet.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT).contains(role))) {
            return true;
        }
        if (requestUserRoles.stream().anyMatch(this.targetRoleSet::contains)) {
            return true;
        }

        throw new UnauthorizedException(
                ErrorCode.API_NOT_ALLOWED,
                "접근 권한이 없습니다."
        );
    }
}
