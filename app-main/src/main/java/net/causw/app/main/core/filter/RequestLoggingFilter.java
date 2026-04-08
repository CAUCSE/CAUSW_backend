package net.causw.app.main.core.filter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * /api/v1, /api/v2 경로의 모든 HTTP 요청을 가로채 MDC(Mapped Diagnostic Context)에
 * 요청 정보를 설정하고, 처리 완료 후 로그를 남기는 필터.
 *
 * MDC에 저장된 값들은 logback 패턴의 %X{key} 로 로그에 자동 포함된다.
 * - traceId   : 요청 추적용 고유 ID (타임스탬프 + 요청 순번 조합)
 * - path      : 요청 URI
 * - httpMethod: HTTP 메서드 (GET, POST, ...)
 * - remoteIP  : 클라이언트 IP (X-Forwarded-For 우선)
 * - userId    : 인증된 사용자 이름 (미인증 시 미설정)
 * - status    : HTTP 응답 코드
 * - duration  : 처리 시간 (ms)
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

	// 앱 기동 후 누적되는 요청 순번 — traceId 생성에 사용
	private static final AtomicLong requestCounter = new AtomicLong();

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain)
		throws ServletException, IOException {

		long requestId = requestCounter.incrementAndGet();
		long start = System.currentTimeMillis();

		// 응답 상태 코드를 가로채기 위해 response 래핑
		StatusCaptureWrapper wrappedResponse = new StatusCaptureWrapper(response);

		String requestURI = request.getRequestURI();
		String method = request.getMethod();

		try {
			// traceId: 같은 요청에 속한 로그를 묶어서 추적하기 위한 식별자
			// 형식: (현재 시각 끝 5자리)-(요청 순번 끝 4자리), 예) 23456-0001
			String traceId = String.format("%d-%04d",
				start % 100000,
				Math.abs(requestId % 10000));
			MDC.put("traceId", traceId);
			MDC.put("path", requestURI);
			MDC.put("httpMethod", method);
			MDC.put("remoteIP", getClientIp(request));

			// 인증된 사용자인 경우에만 userId를 MDC에 추가
			String userId = getUserId();
			if (userId != null)
				MDC.put("userId", userId);

			filterChain.doFilter(request, wrappedResponse);
		} finally {
			// 예외 발생 여부와 관계없이 항상 로그를 남기고 MDC를 초기화
			// MDC를 clear하지 않으면 스레드 풀 재사용 시 이전 요청 정보가 남아 오염됨
			long duration = System.currentTimeMillis() - start;
			int status = wrappedResponse.getStatus();
			MDC.put("status", String.valueOf(status));
			MDC.put("duration", String.valueOf(duration));

			// 응답 상태 코드에 따라 로그 레벨 분기
			if (status >= 500) {
				log.error("Request processed [{} {}] status={} duration={}ms", method, requestURI, status, duration);
			} else if (status >= 400) {
				log.warn("Request processed [{} {}] status={} duration={}ms", method, requestURI, status, duration);
			} else {
				log.info("Request processed [{} {}] status={} duration={}ms", method, requestURI, status, duration);
			}

			MDC.clear();
		}
	}

	/**
	 * 로깅 대상에서 제외할 요청을 판별한다.
	 * - OPTIONS 메서드: CORS preflight 요청으로 로깅 불필요
	 * - /api/v1, /api/v2 외 경로: 정적 리소스 등 API 외 요청
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String method = request.getMethod();

		if ("OPTIONS".equals(method)) {
			return true;
		}

		return !(uri.toLowerCase().startsWith("/api/v1") || uri.toLowerCase().startsWith("/api/v2"));
	}

	/**
	 * 실제 클라이언트 IP를 반환한다.
	 * 로드밸런서/리버스 프록시 환경에서는 X-Forwarded-For 헤더에 원본 IP가 담기므로 우선 확인한다.
	 * 헤더에 여러 IP가 콤마로 나열된 경우 가장 앞에 있는 값이 최초 클라이언트 IP다.
	 */
	private String getClientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	/**
	 * 현재 요청의 인증된 사용자 이름을 반환한다.
	 * 미인증 또는 익명 사용자(AnonymousAuthenticationToken)이면 null을 반환한다.
	 * AnonymousAuthenticationToken은 isAuthenticated()가 true이므로 별도로 제외한다.
	 */
	private String getUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
			return null;
		}
		return auth.getName();
	}
}
