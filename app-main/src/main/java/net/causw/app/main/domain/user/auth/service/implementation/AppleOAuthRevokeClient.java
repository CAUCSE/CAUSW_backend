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

	// 웹(apple) 설정
	@Value("${spring.security.oauth2.client.registration.apple.client-id}")
	private String webClientId;

	@Value("${spring.security.oauth2.client.registration.apple.client-secret}")
	private String webClientSecret;

	// 앱(apple-ios) 설정
	@Value("${spring.security.oauth2.client.registration.apple-ios.client-id}")
	private String iosClientId;

	@Value("${spring.security.oauth2.client.registration.apple-ios.client-secret}")
	private String iosClientSecret;

	/**
	 * Apple 연동 해제를 수행합니다.
	 *
	 * @param refreshToken 소셜 제공자로부터 발급받은 리프레시 토큰
	 * @param platformHint API를 통해 전달받은 플랫폼 힌트 (예: "ios", "web") 또는 null (관리자 추방 시)
	 */
	public void revoke(String refreshToken, String platformHint) {
		boolean isIosHint = "ios".equalsIgnoreCase(platformHint) || "apple-ios".equalsIgnoreCase(platformHint);
		boolean isWebHint = "web".equalsIgnoreCase(platformHint) || "apple".equalsIgnoreCase(platformHint);

		if (isIosHint) {
			if (tryRevoke(iosClientId, iosClientSecret, refreshToken, "iOS 앱"))
				return;
			log.warn("[Apple Revoke] iOS 앱 설정 해제 실패. 웹 설정으로 재시도합니다.");
			if (tryRevoke(webClientId, webClientSecret, refreshToken, "Web"))
				return;

		} else if (isWebHint) {
			if (tryRevoke(webClientId, webClientSecret, refreshToken, "Web"))
				return;
			log.warn("[Apple Revoke] 웹 설정 해제 실패. iOS 앱 설정으로 재시도합니다.");
			if (tryRevoke(iosClientId, iosClientSecret, refreshToken, "iOS 앱"))
				return;

		} else {
			// 힌트가 없는 경우 (예: 관리자 추방), iOS부터 시도
			log.info("[Apple Revoke] 플랫폼 힌트 없음. iOS 앱 설정부터 순차적으로 시도합니다.");
			if (tryRevoke(iosClientId, iosClientSecret, refreshToken, "iOS 앱"))
				return;
			if (tryRevoke(webClientId, webClientSecret, refreshToken, "Web"))
				return;
		}

		// 두 설정 모두 실패한 경우
		log.error("[Apple Revoke] 모든 플랫폼(Web, App) 설정으로 연동 해제에 실패했습니다.");
		throw new RuntimeException("Apple token revoke failed for all platforms.");
	}

	/**
	 * 실제 Apple Revoke API를 호출하는 헬퍼 메서드
	 * @return 성공 시 true, 실패 시 false
	 */
	private boolean tryRevoke(String clientId, String clientSecret, String refreshToken, String platformName) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("client_id", clientId);
		form.add("client_secret", clientSecret);
		form.add("token", refreshToken);
		form.add("token_type_hint", "refresh_token");

		try {
			restClient.post()
				.uri("https://appleid.apple.com/auth/revoke")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.toBodilessEntity();

			log.info("[Apple Revoke] 성공 (플랫폼: {}, client_id: {})", platformName, clientId);
			return true;
		} catch (Exception e) {
			log.warn("[Apple Revoke] 시도 실패 (플랫폼: {}, client_id: {}), 사유: {}", platformName, clientId, e.getMessage());
			return false;
		}
	}
}
