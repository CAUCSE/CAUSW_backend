package net.causw.domain.validation.valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;
import net.causw.domain.validation.GrantableRoleValidator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class AdminValidAspect {

    private final GrantableRoleValidator grantableRoleValidator;

    @Pointcut("@annotation(net.causw.domain.validation.valid.AdminValid)")
    public void pointCut() {}

    @AfterReturning(value = "pointCut()", returning = "user")
    public void validUserMethod(JoinPoint joinPoint, User user) {
        Object[] parameters = joinPoint.getArgs(); // 메서드의 파라미터

        Set<Role> granterRoles = null;
        Role targetRole = null;

        for (Object parameter : parameters) {
            if (parameter instanceof Set) {
                if (!((Set<?>) parameter).isEmpty() && ((Set<?>) parameter).iterator().next() instanceof Role) {
                    granterRoles = (Set<Role>) parameter;
                }
            } else if (parameter instanceof Role) {
                targetRole = (Role) parameter;
            }
        }

        grantableRoleValidator.setRoles(granterRoles, targetRole);
        grantableRoleValidator.isValid(user, null);
    }
}
