package net.causw.domain.validation.valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.domain.validation.ImageLocationTypeValidator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class UtilValidAspect {
    private final ImageLocationTypeValidator imageLocationTypeValidator;

    @Pointcut("execution(* *(.., @net.causw.domain.validation.valid.UtilValid (*), ..))")
    public void enableValid() {}

    @Before("enableValid()")
    public void validUserParameter(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof UtilValid utilValid) {
                    if (args[i] instanceof String type) {
                        if (utilValid.ImageLocationTypeValidator()) {
                            imageLocationTypeValidator.isValid(type, null);
                        }
                    }
                }
            }
        }
    }
}
