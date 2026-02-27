package net.causw.app.main.core.security;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AppleOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
	private static final String APPLE_REGISTRATION_ID = "apple";
	private static final String RESPONSE_MODE_PARAMETER_NAME = "response_mode";
	private static final String RESPONSE_MODE_FORM_POST = "form_post";
	private static final String DEFAULT_AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization";

	private final OAuth2AuthorizationRequestResolver delegate;

	public AppleOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
		this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
			clientRegistrationRepository,
			DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		return customizeIfApple(delegate.resolve(request));
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
		return customizeIfApple(delegate.resolve(request, clientRegistrationId));
	}

	private OAuth2AuthorizationRequest customizeIfApple(OAuth2AuthorizationRequest authorizationRequest) {
		if (authorizationRequest == null) {
			return null;
		}

		String registrationId = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
		if (!APPLE_REGISTRATION_ID.equalsIgnoreCase(registrationId)) {
			return authorizationRequest;
		}

		Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
		additionalParameters.put(RESPONSE_MODE_PARAMETER_NAME, RESPONSE_MODE_FORM_POST);

		return OAuth2AuthorizationRequest.from(authorizationRequest)
			.additionalParameters(additionalParameters)
			.build();
	}
}
