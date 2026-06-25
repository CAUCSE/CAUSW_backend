package net.causw.app.main.domain.user.auth.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.ObjectMapper;

class OidcAuthorizationCodeTokenClientTest {

	@Test
	@DisplayName("native/login authorization code 교환 요청에는 redirect_uri를 포함하지 않는다")
	void buildTokenRequestForm_doesNotIncludeRedirectUri() {
		OidcAuthorizationCodeTokenClient client = new OidcAuthorizationCodeTokenClient(new ObjectMapper(),
			RestClient.builder());

		ClientRegistration registration = ClientRegistration.withRegistrationId("google")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.clientId("google-client-id")
			.clientSecret("google-client-secret")
			.authorizationUri("https://example.com/oauth/authorize")
			.tokenUri("https://example.com/oauth/token")
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.scope("openid", "email", "profile")
			.clientName("google")
			.build();

		MultiValueMap<String, String> form = client.buildTokenRequestForm(registration, "auth-code", null);

		assertThat(form).containsEntry("grant_type", java.util.List.of("authorization_code"));
		assertThat(form).containsEntry("code", java.util.List.of("auth-code"));
		assertThat(form).containsEntry("client_id", java.util.List.of("google-client-id"));
		assertThat(form).containsEntry("client_secret", java.util.List.of("google-client-secret"));
		assertThat(form).doesNotContainKey("redirect_uri");
	}
}
