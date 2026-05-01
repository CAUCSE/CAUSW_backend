package net.causw.app.main.domain.user.auth.service.implementation;

import org.springframework.beans.factory.annotation.Value;
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

	@Value("${spring.security.oauth2.client.registration.kakao.admin-key}")
	private String adminKey;

	/**
	 * 어드민 키를 사용한 강제 연동 해제 (Fallback)
	 * @param targetId 카카오 앱 유저 ID (SocialAccount에 저장된 식별자)
	 */
	public void unlinkWithAdminKey(String targetId) {
		restClient.post()
			.uri("https://kapi.kakao.com/v1/user/unlink")
			.header(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body("target_id_type=user_id&target_id=" + targetId)
			.retrieve()
			.toBodilessEntity();

		log.info("Kakao account unlinked successfully using Admin Key. Target ID: {}", targetId);
	}
}
