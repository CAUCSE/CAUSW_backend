package net.causw.app.main.core.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.auth.util.OAuthRedirectResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
/**
 * OAuth2 AuthorizationRequest 저장소를 감싸서 env 쿠키를 함께 관리하는 리포지토리입니다.
 * <p>
 * 기본 저장/조회/삭제는 세션 기반 delegate에 위임하고,
 * 저장 시점에만 요청 파라미터(env)를 읽어 OAuthRedirectResolver를 통해 쿠키를 설정합니다.
 */
public class OAuth2AuthorizationRequestCookieRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
	private final HttpSessionOAuth2AuthorizationRequestRepository delegate = new HttpSessionOAuth2AuthorizationRequestRepository();
	private final OAuthRedirectResolver oAuthRedirectResolver;

	/**
	 * 세션에 저장된 OAuth2 AuthorizationRequest를 조회합니다.
	 *
	 * @param request 현재 HTTP 요청
	 * @return 저장된 AuthorizationRequest, 없으면 null
	 */
	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return delegate.loadAuthorizationRequest(request);
	}

	/**
	 * AuthorizationRequest 저장 전에 env 쿠키를 갱신한 뒤 요청 상태를 저장합니다.
	 *
	 * @param authorizationRequest OAuth2 AuthorizationRequest
	 * @param request 현재 HTTP 요청
	 * @param response 현재 HTTP 응답
	 */
	@Override
	public void saveAuthorizationRequest(
		OAuth2AuthorizationRequest authorizationRequest,
		HttpServletRequest request,
		HttpServletResponse response) {
		ResponseCookie envCookie = resolveEnvCookie(request);
		response.addHeader(HttpHeaders.SET_COOKIE, envCookie.toString());
		delegate.saveAuthorizationRequest(authorizationRequest, request, response);
	}

	/**
	 * 세션에 저장된 OAuth2 AuthorizationRequest를 제거하고 반환합니다.
	 *
	 * @param request 현재 HTTP 요청
	 * @param response 현재 HTTP 응답
	 * @return 제거된 AuthorizationRequest, 없으면 null
	 */
	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		return delegate.removeAuthorizationRequest(request, response);
	}

	private ResponseCookie resolveEnvCookie(HttpServletRequest request) {
		String env = request.getParameter("env");
		if (oAuthRedirectResolver.isSupportedEnv(env)) {
			return oAuthRedirectResolver.createEnvCookie(env, request);
		}
		return oAuthRedirectResolver.clearEnvCookie(request);
	}
}
