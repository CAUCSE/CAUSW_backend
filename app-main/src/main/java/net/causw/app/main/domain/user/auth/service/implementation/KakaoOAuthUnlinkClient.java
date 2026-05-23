package net.causw.app.main.domain.user.auth.service.implementation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthUnlinkClient {

	private final RestClient restClient = RestClient.create();

	@Value("${kakao.admin-key}")
	private String adminKey;

	/**
	 * 어드민 키를 사용한 강제 연동 해제 (Fallback)
	 * @param targetId 카카오 앱 유저 ID (SocialAccount에 저장된 식별자)
	 */
	public void unlinkWithAdminKey(String targetId) {
		try {
			restClient.post()
				.uri("https://kapi.kakao.com/v1/user/unlink")
				.header(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body("target_id_type=user_id&target_id=" + targetId)
				.retrieve()
				.toBodilessEntity();

			log.info("[Kakao Unlink] 어드민 키를 활용한 연동 해제 성공. Target ID: {}", targetId);
		} catch (Exception e) {
			log.error("[Kakao Unlink] 연동 해제 실패. Target ID: {}, Message: {}", targetId, e.getMessage());
			throw AuthErrorCode.KAKAO_UNLINK_FAILED.toBaseException();
		}

	}
}
