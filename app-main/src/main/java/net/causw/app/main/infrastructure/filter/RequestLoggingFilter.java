package net.causw.app.main.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final AtomicLong requestCounter = new AtomicLong();

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    long requestId = requestCounter.incrementAndGet();
    try {
      String traceId = String.format("%d-%04d",
          System.currentTimeMillis() % 100000,
          requestId % 10000);
      MDC.put("traceId", traceId);
      String requestURI = request.getRequestURI();
      MDC.put("path", requestURI);
      String method = request.getMethod();
      MDC.put("httpMethod", method);
      MDC.put("remoteIP", request.getRemoteAddr());

      String userId = getUserId();
      if (userId != null) MDC.put("userId", userId);

      long start = System.currentTimeMillis();

      StatusCaptureWrapper wrappedResponse = new StatusCaptureWrapper(response);
      filterChain.doFilter(request, wrappedResponse);

      long duration = System.currentTimeMillis() - start;
      MDC.put("status", String.valueOf(wrappedResponse.getStatus()));
      MDC.put("duration", String.valueOf(duration));

      log.info("Request processed API URI: " + "[" + method + "] " + requestURI);
    } finally {
      MDC.clear(); // 누락되면 memory leak
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String method = request.getMethod();

    // HTTP 메서드로 제외
    if ("OPTIONS".equals(method)) {
      return true;
    }

    return uri.startsWith("/static/")
        || uri.startsWith("/actuator/")
        || uri.equals("/favicon.ico")
        || uri.equals("/healthy")
        || uri.equals("/robots.txt");
  }

  private String getUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
  }
}
