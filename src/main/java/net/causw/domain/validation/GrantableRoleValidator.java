package net.causw.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.validation.valid.AdminValid;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class GrantableRoleValidator implements ConstraintValidator<AdminValid, User> {

    private Set<Role> grantorRoles;
    private Role grantedRole;

    public void setRoles(Set<Role> grantorRoles, Role grantedRole) {
        this.grantorRoles = grantorRoles;
        this.grantedRole = grantedRole;
    }

    public void validate(Set<Role> grantorRoles, Role grantedRole, Set<Role> granteeRoles) {
        /* When role of grantor is Admin
         * Granted and grantee role should not be Admin
         * Granted and grantee role should be different
         * Grantee role should not be Leader Circle or Leader Alumni
         *   => They will automatically granted while other granting process since the roles has unique user
         */
        if (grantorRoles.contains(Role.ADMIN)) {
            if (!granteeRoles.contains(Role.ADMIN)) {
                if (grantedRole.equals(Role.LEADER_CIRCLE) || granteeRoles.contains(Role.LEADER_CIRCLE)) {
                    return;
                } else if (granteeRoles.contains(Role.COMMON)) {
                    return;
                } else if (grantedRole.equals(Role.COMMON)) {
                    return;
                }
            }
        }
        /* When role of grantor is President
         * Granted role should not be Admin, and Grantee role should not be Admin and President
         * Granted and grantee role should be different
         * Grantee role should not be Leader Circle or Leader Alumni
         *   => They will automatically granted while other granting process since the roles has unique user
         */
        else if (grantorRoles.contains(Role.PRESIDENT)) {
            if (grantedRole != Role.ADMIN
                    && (!granteeRoles.contains(Role.ADMIN) && !granteeRoles.contains(Role.PRESIDENT))) {
                if (grantedRole.equals(Role.LEADER_CIRCLE) || granteeRoles.contains(Role.LEADER_CIRCLE)) {
                    return;
                } else if (granteeRoles.contains(Role.COMMON)) {
                    return;
                } else if (grantedRole.equals(Role.COMMON)) {
                    return;
                }
            }
        }
        /* When role of grantor is Leader_Circle
         * Granted role should be Leader_Circle, and Grantee role should be Common
         */
        else if (grantorRoles.contains(Role.LEADER_CIRCLE)) {
            if (grantedRole.equals(Role.LEADER_CIRCLE)) {
                if (!granteeRoles.contains(Role.ADMIN) && !granteeRoles.contains(Role.PRESIDENT) && !granteeRoles.contains(Role.VICE_PRESIDENT)
                        && !granteeRoles.contains(Role.LEADER_ALUMNI) && !granteeRoles.contains(Role.PROFESSOR)) {
                    return;
                }
            }
        }
        /* When role of grantor is Leader_Alumni
         * Granted role should be Leader_Alumni, and Grantee role should be Common
         */
        else if (grantorRoles.contains(Role.LEADER_ALUMNI)) {
            if (grantedRole == Role.LEADER_ALUMNI && granteeRoles.contains(Role.COMMON)) {
                return;
            }
        }

        throw new UnauthorizedException(
                ErrorCode.GRANT_ROLE_NOT_ALLOWED,
                String.format("권한을 부여할 수 없습니다.")
        );
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        validate(grantorRoles, grantedRole, user.getRoles());
        return true;
    }
}
