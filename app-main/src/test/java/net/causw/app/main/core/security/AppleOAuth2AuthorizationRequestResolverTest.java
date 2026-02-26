package net.causw.app.main.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;

class AppleOAuth2AuthorizationRequestResolverTest {

	@Test
	@DisplayName("apple authorization 요청에는 response_mode=form_post를 추가한다")
	void resolve_AppleRegistration_AddsFormPostResponseMode() {
		AppleOAuth2AuthorizationRequestResolver resolver = new AppleOAuth2AuthorizationRequestResolver(
			new InMemoryClientRegistrationRepository(appleClientRegistration(), googleClientRegistration()));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");

		OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request, "apple");

		assertNotNull(authorizationRequest);
		Map<String, Object> additionalParameters = authorizationRequest.getAdditionalParameters();
		assertEquals("form_post", additionalParameters.get("response_mode"));
		assertNotNull(additionalParameters.get(OidcParameterNames.NONCE));
	}

	@Test
	@DisplayName("apple이 아닌 요청에는 response_mode를 추가하지 않는다")
	void resolve_NonAppleRegistration_DoesNotAddResponseMode() {
		AppleOAuth2AuthorizationRequestResolver resolver = new AppleOAuth2AuthorizationRequestResolver(
			new InMemoryClientRegistrationRepository(appleClientRegistration(), googleClientRegistration()));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");

		OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request, "google");

		assertNotNull(authorizationRequest);
		assertNull(authorizationRequest.getAdditionalParameters().get("response_mode"));
	}

	private ClientRegistration appleClientRegistration() {
		return ClientRegistration.withRegistrationId("apple")
			.clientId("apple-client")
			.clientSecret("apple-secret")
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.scope("openid", "email", "name")
			.authorizationUri("https://appleid.apple.com/auth/authorize")
			.tokenUri("https://appleid.apple.com/auth/token")
			.jwkSetUri("https://appleid.apple.com/auth/keys")
			.userNameAttributeName("sub")
			.clientName("Apple")
			.build();
	}

	private ClientRegistration googleClientRegistration() {
		return ClientRegistration.withRegistrationId("google")
			.clientId("google-client")
			.clientSecret("google-secret")
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.scope("openid", "profile", "email")
			.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
			.tokenUri("https://oauth2.googleapis.com/token")
			.jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
			.userNameAttributeName("sub")
			.clientName("Google")
			.build();
	}
}
