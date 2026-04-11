package net.causw.app.main.domain.user.auth.service.implementation;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GoogleOAuthRevokeClient {

	private final RestClient restClient = RestClient.create();

	public void revoke(String refreshToken) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("token", refreshToken);

		restClient.post()
			.uri("https://oauth2.googleapis.com/revoke")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(form)
			.retrieve()
			.toBodilessEntity();

		log.info("Google token revoked successfully");
	}
}
