package net.causw.domain.validation.valid;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserNotEqualValidator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
@RequiredArgsConstructor
public class CircleValidAspect {
    private final UserEqualValidator userEqualValidator;
    private final UserNotEqualValidator userNotEqualValidator;

    @Pointcut("@annotation(net.causw.domain.validation.valid.CircleValid)")
    public void pointCut() {}

    @AfterReturning(value = "pointCut()", returning = "circle")
    public void validUserMethod(JoinPoint joinPoint, Circle circle) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] parameters = joinPoint.getArgs(); // 메서드의 파라미터

        CircleValid circleValid = method.getAnnotation(CircleValid.class);

        if (circleValid.UserEqualValidator()) {
            for (Object parameter : parameters) {
                if (parameter instanceof User paramUser) {
                    String userId = paramUser.getId();
                    String leaderId = circle.getLeader().orElseThrow(
                            () -> new InternalServerException(
                                    ErrorCode.INTERNAL_SERVER,
                                    MessageUtil.CIRCLE_WITHOUT_LEADER
                            )
                    ).getId();
                    userEqualValidator.validate(userId, leaderId);
                }
            }
        }
        if (circleValid.UserNotEqualValidator()) {
            for (Object parameter : parameters) {
                if (parameter instanceof User paramUser) {
                    String userId = paramUser.getId();
                    String leaderId = circle.getLeader().orElseThrow(
                            () -> new InternalServerException(
                                    ErrorCode.INTERNAL_SERVER,
                                    MessageUtil.CIRCLE_WITHOUT_LEADER
                            )
                    ).getId();
                    userNotEqualValidator.validate(userId, leaderId);
                }
            }
        }
    }
}
