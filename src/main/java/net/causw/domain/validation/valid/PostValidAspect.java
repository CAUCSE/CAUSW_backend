package net.causw.domain.validation.valid;

import lombok.RequiredArgsConstructor;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.validation.PostNumberOfAttachmentsValidator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

@Component
@Aspect
@RequiredArgsConstructor
public class PostValidAspect {

    @Pointcut("execution(* *(.., @net.causw.domain.validation.valid.PostValid (*), ..))")
    public void enableValid() {}

    @Before("enableValid()")
    public void validPostParameter(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof PostValid postValid) {
                    Object object = args[i];

                    if (postValid.PostNumberOfAttachmentsValidator()) {
                        Method getAttachmentsListMethod = null;
                        try {
                            getAttachmentsListMethod = object.getClass().getMethod("getAttachmentList");
                            List<String> attachmentsList = (List<String>) getAttachmentsListMethod.invoke(object);
                            new PostNumberOfAttachmentsValidator().isValid(attachmentsList); // validate !!
                        } catch (Exception e) {
                            throw new BadRequestException(
                                    ErrorCode.VALUE_NOT_EXIST,
                                    "첨부 파일 목록을 찾을 수 없습니다."
                            );
                        }
                    }
                }
            }
        }
    }
}
