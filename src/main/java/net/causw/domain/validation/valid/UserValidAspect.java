package net.causw.domain.validation.valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;
import net.causw.domain.validation.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class UserValidAspect {
    private final UserRoleIsNoneValidator userRoleIsNoneValidator;
    private final UserStateValidator userStateValidator;
    private final UserRoleValidator userRoleValidator;
    private final UserRoleWithoutAdminValidator userRoleWithoutAdminValidator;
    private final UserStateIsDropOrIsInActiveValidator userStateIsDropOrIsInActiveValidator;
    private final UserStateIsNotDropAndActiveValidator userStateIsNotDropAndActiveValidator;
    private final UserEqualValidator userEqualValidator;
    private final UserNotEqualValidator userNotEqualValidator;

    @Pointcut("@annotation(net.causw.domain.validation.valid.UserValid)")
    public void pointCut() {}

    @Pointcut("execution(* *(.., @net.causw.domain.validation.valid.UserValid (*), ..))")
    public void enableValid() {}

    // Method
    @AfterReturning(value = "pointCut()", returning = "user")
    public void validUserMethod(JoinPoint joinPoint, User user) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] parameters = joinPoint.getArgs(); // 메서드의 파라미터

        UserValid userValid = method.getAnnotation(UserValid.class);

        if (userValid.UserRolesIsNoneValidator()) {
            userRoleIsNoneValidator.isValid(user, null);
        }
        if (userValid.UserStateValidator()) {
            userStateValidator.isValid(user, null);
        }
    }

    // Parameter
    @Before("enableValid()")
    public void validUserParameter(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof UserValid userValid) {
                    if (args[i] instanceof User user) {
                        if (userValid.UserRolesIsNoneValidator()) {
                            userRoleIsNoneValidator.isValid(user, null);
                        }
                        if (userValid.UserStateValidator()) {
                            userStateValidator.isValid(user, null);
                        }
                        if (userValid.UserRoleValidator()) {
                            Set<Role> targetRoleSet = Stream.of(userValid.targetRoleSet())
                                    .map(String::trim)
                                    .map(Role::valueOf)
                                    .collect(Collectors.toSet());
                            userRoleValidator.setTargetRoleSet(targetRoleSet);
                            userRoleValidator.isValid(user, null);
                        }
                        if (userValid.UserRoleWithoutAdminValidator()) {
                            userRoleWithoutAdminValidator.isValid(user, null);
                        }
                        if (userValid.UserStateIsDropOrIsInActiveValidator()) {
                            userStateIsDropOrIsInActiveValidator.isValid(user, null);
                        }
                        if (userValid.UserStateIsNotDropAndActiveValidator()) {
                            userStateIsNotDropAndActiveValidator.isValid(user, null);
                        }
                        if (userValid.UserEqualValidator()) {
                            userEqualValidator.setTargetUserId(userValid.targetUserId());
                            userEqualValidator.isValid(user, null);
                        }
                        if (userValid.UserNotEqualValidator()) {
                            userNotEqualValidator.setTargetUserId(userValid.targetUserId());
                            userNotEqualValidator.isValid(user, null);
                        }
                    }
                }
            }
        }
    }

}
