package net.causw.app.main.domain.user.auth.service.implementation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthUnlinkClient {

	private final RestClient restClient = RestClient.create();

	public void unlink(String token) {
		restClient.post()
			.uri("https://kapi.kakao.com/v1/user/unlink")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.retrieve()
			.toBodilessEntity();

		log.info("Kakao account unlinked successfully.");
	}
}
