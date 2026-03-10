package net.causw.app.main.core.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import net.causw.app.main.domain.user.auth.util.OAuthRedirectResolver;

class OAuth2AuthorizationRequestCookieRepositoryTest {
	private final OAuth2AuthorizationRequestCookieRepository repository = new OAuth2AuthorizationRequestCookieRepository(
		new OAuthRedirectResolver());

	@Test
	@DisplayName("env가 local이면 oauth_env 쿠키를 저장한다")
	void saveAuthorizationRequest_WhenLocalEnv_SavesEnvCookie() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/kakao");
		request.setParameter("env", "local");
		request.setParameter("state", "state-123");
		MockHttpServletResponse response = new MockHttpServletResponse();

		repository.saveAuthorizationRequest(createAuthorizationRequest(), request, response);

		String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
		assertThat(setCookie).contains("oauth_env=local");
		assertThat(setCookie).contains("Max-Age=180");
		assertThat(repository.loadAuthorizationRequest(request)).isNotNull();
	}

	@Test
	@DisplayName("지원하지 않는 env면 oauth_env 쿠키를 즉시 만료시킨다")
	void saveAuthorizationRequest_WhenUnsupportedEnv_ExpiresEnvCookie() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/kakao");
		request.setParameter("env", "prod");
		MockHttpServletResponse response = new MockHttpServletResponse();

		repository.saveAuthorizationRequest(createAuthorizationRequest(), request, response);

		String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
		assertThat(setCookie).contains("oauth_env=");
		assertThat(setCookie).contains("Max-Age=0");
	}

	@Test
	@DisplayName("removeAuthorizationRequest는 저장된 OAuth2 요청을 반환하고 제거한다")
	void removeAuthorizationRequest_RemovesStoredRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/kakao");
		request.setParameter("env", "local");
		request.setParameter("state", "state-123");
		MockHttpServletResponse response = new MockHttpServletResponse();
		OAuth2AuthorizationRequest authorizationRequest = createAuthorizationRequest();

		repository.saveAuthorizationRequest(authorizationRequest, request, response);
		OAuth2AuthorizationRequest removed = repository.removeAuthorizationRequest(request, response);

		assertThat(removed).isNotNull();
		assertThat(removed.getState()).isEqualTo(authorizationRequest.getState());
		assertThat(repository.loadAuthorizationRequest(request)).isNull();
	}

	private OAuth2AuthorizationRequest createAuthorizationRequest() {
		return OAuth2AuthorizationRequest.authorizationCode()
			.authorizationUri("https://kauth.kakao.com/oauth/authorize")
			.clientId("kakao-client")
			.redirectUri("http://localhost:8080/login/oauth2/code/kakao")
			.state("state-123")
			.authorizationRequestUri("https://kauth.kakao.com/oauth/authorize?state=state-123")
			.build();
	}
}
