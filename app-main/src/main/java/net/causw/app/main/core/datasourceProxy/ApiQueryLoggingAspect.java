package net.causw.app.main.core.datasourceProxy;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.causw.app.main.core.datasourceProxy.QueryContext.QueryInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class ApiQueryLoggingAspect {

    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
        "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
        "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
        "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object logApiQueries(ProceedingJoinPoint joinPoint) throws Throwable {
        QueryContext.clear();
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = "";
        String path = "";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            method = request.getMethod();
            path = request.getRequestURI();
        }

        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            long totalTime = System.currentTimeMillis() - startTime;
            List<QueryInfo> queries = QueryContext.getQueries();
            long totalQueryTime = queries.stream()
                .mapToLong(QueryContext.QueryInfo::getExecutionTime)
                .sum();

            log.info("=== API Performance : {} {}  ===", method, path);
            log.info("Total: {}ms | Query: {}ms | Overhead: {}ms | Query Count : {}",
                totalTime,
                totalQueryTime,
                totalTime-totalQueryTime,
                queries.size()
            );

            if(totalTime > 1000){
                log.warn("Slow API Detected: {} {} | Time: {}", method, path, totalTime);
            }

            if(queries.size()>10){ // TODO: 하드코딩 제거 필요
                log.warn("Too Much Queries: {} {} | Count: {}", method, path, queries.size());
            }

            // 쿼리별 상세 정보 (선택적)
            for (int i = 0; i < queries.size(); i++) {
                QueryContext.QueryInfo q = queries.get(i);
                log.debug("Query Detail #{}: {}ms - {}",
                    i + 1, q.getExecutionTime(), q.getQuery());
            }

            QueryContext.clear();
        }
    }
}
