package net.causw.app.main.core.security;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import net.causw.app.main.shared.util.AuthorizationExtractor;
import net.causw.global.exception.UnauthorizedException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * JWT 검증 필터를 거치지 않고 통과시킬 요청 경로를 설정합니다.
	 *
	 * <p>토큰 재발급 API({@code /api/v2/auth/refresh})는 Access Token이 이미 만료되었거나,
	 * 소셜 로그인 직후 최초 발급을 위해 호출되는 엔드포인트이므로 토큰 유효성 검사를 수행하지 않습니다.
	 * * <p>※ 참고: 해당 엔드포인트의 CSRF 방어를 위한 커스텀 헤더 검증이 필요하기 때문에 필터 통과 메서드를 추가하였습니다.
	 * 검증은 필터가 아닌 컨트롤러 단에서 {@code AuthorizationExtractor}를 통해 별도로 수행됩니다.
	 *
	 * @param request 현재 HTTP 요청
	 * @return 필터 실행을 생략할지 여부 (재발급 API 경로인 경우 true 반환)
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/api/v2/auth/refresh") || path.startsWith("/login/oauth2/code/");
	}

	@Override
	protected void doFilterInternal(@NotNull HttpServletRequest request,
		@NotNull HttpServletResponse response,
		@NotNull FilterChain chain) throws ServletException, IOException {
		String token = AuthorizationExtractor.extract(request);

		if (StringUtils.hasText(token)) {
			try {
				if (jwtTokenProvider.validateToken(token)) {
					Authentication auth = jwtTokenProvider.getAuthentication(token);
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (UnauthorizedException exception) {
				SecurityContextHolder.clearContext();
				request.setAttribute("exception", exception);

				throw exception;
			}
		}

		chain.doFilter(request, response);
	}
}
