package net.causw.app.main.domain.user.auth.service.implementation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppleOAuthRevokeClient {

	private final RestClient restClient = RestClient.create();

	@Value("${spring.security.oauth2.client.registration.apple.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.apple.client-secret}")
	private String clientSecret;

	public void revoke(String refreshToken) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("client_id", clientId);
		form.add("client_secret", clientSecret);
		form.add("token", refreshToken);
		form.add("token_type_hint", "refresh_token");

		restClient.post()
			.uri("https://appleid.apple.com/auth/revoke")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(form)
			.retrieve()
			.toBodilessEntity();

		log.info("Apple token revoked successfully");
	}
}
