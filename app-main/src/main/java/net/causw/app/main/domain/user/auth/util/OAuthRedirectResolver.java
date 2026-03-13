package net.causw.app.main.domain.user.auth.util;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
/**
 * OAuth 로그인 전/후 리다이렉트 목적지와 env 쿠키를 관리하는 유틸 컴포넌트입니다.
 * <p>
 * 지원하는 env(local/dev)는 짧은 수명의 HttpOnly 쿠키로 저장되고,
 * 콜백 시 해당 쿠키를 읽어 최종 프론트 리다이렉트 주소를 결정합니다.
 */
public class OAuthRedirectResolver {
	private static final String ENV_COOKIE_NAME = "oauth_env";
	private static final Duration ENV_COOKIE_TTL = Duration.ofMinutes(3);
	private static final Set<String> SUPPORTED_ENVS = Set.of("local", "dev");

	@Value("${app.auth.redirect-uri}")
	private String defaultRedirectUri;

	@Value("${app.auth.redirect-uri-local:}")
	private String localRedirectUri;

	@Value("${app.auth.redirect-uri-dev:}")
	private String devRedirectUri;

	/**
	 * 요청 파라미터로 전달된 env 값이 지원 대상인지 확인합니다.
	 *
	 * @param env 프론트에서 전달한 env 값
	 * @return 지원하는 env(local/dev)면 true
	 */
	public boolean isSupportedEnv(String env) {
		String normalized = normalizeEnv(env);
		return normalized != null && SUPPORTED_ENVS.contains(normalized);
	}

	/**
	 * OAuth 진입 직전에 env 정보를 담은 쿠키를 생성합니다.
	 *
	 * @param env 요청 env 값
	 * @param request 현재 HTTP 요청
	 * @return 3분 TTL의 env 쿠키
	 */
	public ResponseCookie createEnvCookie(String env, HttpServletRequest request) {
		String normalized = normalizeEnv(env);
		return buildEnvCookie(normalized, ENV_COOKIE_TTL, request);
	}

	/**
	 * 사용이 끝난 env 쿠키를 즉시 만료시키는 쿠키를 생성합니다.
	 *
	 * @param request 현재 HTTP 요청
	 * @return 즉시 만료(max-age=0) 쿠키
	 */
	public ResponseCookie clearEnvCookie(HttpServletRequest request) {
		return buildEnvCookie("", Duration.ZERO, request);
	}

	/**
	 * 요청 쿠키에서 env를 읽어 최종 리다이렉트 기준 URL을 반환합니다.
	 * <p>
	 * local/dev 전용 URI가 비어 있거나 설정되지 않은 경우 default URI로 fallback 합니다.
	 *
	 * @param request 현재 HTTP 요청
	 * @return 리다이렉트 기준 URL
	 */
	public String resolveRedirectBase(HttpServletRequest request) {
		return resolveRedirectBase(resolveEnv(request).orElse(null));
	}

	/**
	 * 요청 쿠키에서 env 값을 추출합니다.
	 *
	 * @param request 현재 HTTP 요청
	 * @return 지원 env가 있으면 Optional 값, 없으면 Optional.empty()
	 */
	public Optional<String> resolveEnv(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}

		for (Cookie cookie : cookies) {
			if (ENV_COOKIE_NAME.equals(cookie.getName())) {
				String normalized = normalizeEnv(cookie.getValue());
				return isSupportedEnv(normalized) ? Optional.of(normalized) : Optional.empty();
			}
		}

		return Optional.empty();
	}

	private String resolveRedirectBase(String env) {
		if ("local".equals(env) && hasText(localRedirectUri)) {
			return localRedirectUri;
		}
		if ("dev".equals(env) && hasText(devRedirectUri)) {
			return devRedirectUri;
		}
		return defaultRedirectUri;
	}

	private ResponseCookie buildEnvCookie(String value, Duration maxAge, HttpServletRequest request) {
		CookiePolicy policy = CookiePolicy.from(request);
		return ResponseCookie.from(ENV_COOKIE_NAME, value)
			.httpOnly(false)
			.secure(policy.secure())
			.path("/")
			.maxAge(maxAge)
			.sameSite(policy.sameSite())
			.build();
	}

	private String normalizeEnv(String env) {
		if (env == null) {
			return null;
		}
		String normalized = env.trim().toLowerCase();
		return normalized.isBlank() ? null : normalized;
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private record CookiePolicy(boolean secure, String sameSite) {
		private static CookiePolicy from(HttpServletRequest request) {
			boolean secure = request.isSecure();
			return new CookiePolicy(secure, "None");
		}
	}
}
