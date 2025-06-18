package net.causw.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
      MDC.put("traceId", traceId);
      MDC.put("path", request.getRequestURI());
      MDC.put("httpMethod", request.getMethod());
      MDC.put("remoteIP", request.getRemoteAddr());

      String userId = getUserId();
      if (userId != null) MDC.put("userId", userId);

      long start = System.currentTimeMillis();

      StatusCaptureWrapper wrappedResponse = new StatusCaptureWrapper(response);
      filterChain.doFilter(request, wrappedResponse);

      long duration = System.currentTimeMillis() - start;
      MDC.put("status", String.valueOf(wrappedResponse.getStatus()));
      MDC.put("duration", String.valueOf(duration));

    } finally {
      MDC.clear(); // 누락되면 memory leak
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/static/") || uri.equals("/favicon.ico");
  }

  private String getUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
  }
}
