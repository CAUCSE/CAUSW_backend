package net.causw.app.main.domain.user.auth.service.implementation;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GoogleOAuthRevokeClient {

	private final RestClient restClient = RestClient.create();

	public void revoke(String refreshToken) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("token", refreshToken);

			restClient.post()
				.uri("https://oauth2.googleapis.com/revoke")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.toBodilessEntity();

			log.info("[Google Revoke] 구글 토큰 연동 해제 성공");
		} catch (Exception e) {
			log.error("[Google Revoke] 구글 연동 해제 실패. Message: {}", e.getMessage());
			throw AuthErrorCode.GOOGLE_REVOKE_FAILED.toBaseException();
		}
	}
}
