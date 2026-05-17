package net.causw.app.main.core.security;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.auth.service.implementation.OAuthLinkTokenStore;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AppleOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
	private static final String RESPONSE_MODE_PARAMETER_NAME = "response_mode";
	private static final String RESPONSE_MODE_FORM_POST = "form_post";
	private static final String ACCESS_TYPE_PARAMETER_NAME = "access_type";
	private static final String ACCESS_TYPE_OFFLINE = "offline";
	private static final String PROMPT_PARAMETER_NAME = "prompt";
	private static final String PROMPT_CONSENT = "consent";
	private static final String INCLUDE_GRANTED_SCOPES_PARAMETER_NAME = "include_granted_scopes";
	private static final String INCLUDE_GRANTED_SCOPES_TRUE = "true";
	private static final String DEFAULT_AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization";

	private final OAuth2AuthorizationRequestResolver delegate;

	public AppleOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
		this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
			clientRegistrationRepository,
			DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		return customize(request, delegate.resolve(request));
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
		return customize(request, delegate.resolve(request, clientRegistrationId));
	}

	private OAuth2AuthorizationRequest customize(HttpServletRequest request,
		OAuth2AuthorizationRequest authorizationRequest) {
		if (authorizationRequest == null) {
			return null;
		}

		String registrationId = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
		if (registrationId == null || registrationId.isBlank()) {
			return authorizationRequest;
		}

		Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
		if (SocialType.APPLE.matchesRegistrationId(registrationId)) {
			additionalParameters.put(RESPONSE_MODE_PARAMETER_NAME, RESPONSE_MODE_FORM_POST);
		}
		if (SocialType.GOOGLE.matchesRegistrationId(registrationId)) {
			additionalParameters.put(ACCESS_TYPE_PARAMETER_NAME, ACCESS_TYPE_OFFLINE);
			additionalParameters.put(PROMPT_PARAMETER_NAME, PROMPT_CONSENT);
			additionalParameters.put(INCLUDE_GRANTED_SCOPES_PARAMETER_NAME, INCLUDE_GRANTED_SCOPES_TRUE);
		}

		// 소셜 계정 연동 플로우: linkToken을 attributes에 포함하여 세션에 함께 저장
		// additionalParameters와 달리 attributes는 provider에 전송되지 않음
		Map<String, Object> attributes = new LinkedHashMap<>(authorizationRequest.getAttributes());
		String linkToken = request.getParameter(OAuthLinkTokenStore.LINK_TOKEN_QUERY_PARAM);
		if (linkToken != null && !linkToken.isBlank()) {
			attributes.put(OAuthLinkTokenStore.LINK_TOKEN_ATTR, linkToken);
		}

		return OAuth2AuthorizationRequest.from(authorizationRequest)
			.additionalParameters(additionalParameters)
			.attributes(attributes)
			.build();
	}
}
