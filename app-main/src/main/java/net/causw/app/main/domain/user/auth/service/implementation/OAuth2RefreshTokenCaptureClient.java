package net.causw.app.main.domain.user.auth.service.implementation;

import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * OAuth2 authorization code 교환 응답에서 리프레시 토큰을 꺼내 현재 요청 속성에 저장합니다.
 * <p>
 * {@link net.causw.app.main.domain.user.auth.handler.OAuth2SuccessHandler}에서 Google/Apple만 DB에 반영합니다.
 */
@Component
public class OAuth2RefreshTokenCaptureClient
	implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

	public static final String REQUEST_ATTR_REFRESH_TOKEN = "causw.oauth2.refreshToken";

	private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegate = new DefaultAuthorizationCodeTokenResponseClient();

	@Override
	public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
		OAuth2AccessTokenResponse response = delegate.getTokenResponse(authorizationGrantRequest);
		OAuth2RefreshToken refreshToken = response.getRefreshToken();
		if (refreshToken == null || !StringUtils.hasText(refreshToken.getTokenValue())) {
			return response;
		}
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs instanceof ServletRequestAttributes servletRequestAttributes) {
			HttpServletRequest request = servletRequestAttributes.getRequest();
			request.setAttribute(REQUEST_ATTR_REFRESH_TOKEN, refreshToken.getTokenValue());
		}
		return response;
	}
}
