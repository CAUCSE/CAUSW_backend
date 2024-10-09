package net.causw.domain.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class LogAspect {

    // @MeasureTime 애노테이션이 붙은 클래스의 메서드를 대상으로 설정
    @Pointcut("@within(net.causw.domain.aop.annotation.MeasureTime)")
    private void timer(){}

    // 메서드 실행 전,후로 시간을 측정하고, 실행된 메서드와 실행시간을 로깅
    @Around("timer()")
    public Object loggingExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        Object result = joinPoint.proceed(); // 조인포인트의 메서드 실행
        stopWatch.stop();

        long totalTimeMillis = stopWatch.getTotalTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        log.info("실행된 메서드: {}, 실행시간 = {}ms", methodName, totalTimeMillis);

        return result;
    }
}
