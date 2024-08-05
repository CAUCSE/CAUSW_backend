package net.causw.domain.validation.valid;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.validation.CircleMemberStatusValidator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Component
@Aspect
@RequiredArgsConstructor
public class CircleMemberValidAspect {
    private final CircleMemberStatusValidator circleMemberStatusValidator;

    @Pointcut("@annotation(net.causw.domain.validation.valid.CircleMemberValid)")
    public void pointCut() {}

    @AfterReturning(value = "pointCut()", returning = "circleMember")
    public void validUserMethod(JoinPoint joinPoint, CircleMember circleMember) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] parameters = joinPoint.getArgs(); // 메서드의 파라미터

        CircleMemberValid circleMemberValid = method.getAnnotation(CircleMemberValid.class);

        if (circleMemberValid.CircleMemberStatusValidator()) {
            for (Object parameter : parameters) {
                if (parameter instanceof List<?> paramList) {
                    if (!paramList.isEmpty() && paramList.get(0) instanceof CircleMemberStatus) {
                        List<CircleMemberStatus> list = (List<CircleMemberStatus>) paramList;
                        System.out.println(Arrays.toString(list.toArray()));
                        circleMemberStatusValidator.setStatusList(list);
                        circleMemberStatusValidator.isValid(circleMember, null);
                    }
                }
            }
        }
    }
}
