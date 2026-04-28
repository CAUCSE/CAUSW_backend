package net.causw.app.main.domain.user.auth.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "네이티브 소셜 로그인 요청 DTO")
public record SocialNativeLoginRequest(
	@NotBlank(message = "소셜 로그인 provider를 입력해 주세요.") @Schema(description = "소셜 로그인 provider", example = "google") String provider,

	@Schema(description = "클라이언트 플랫폼 힌트. Apple은 web/ios 등에 매핑된 client registration으로 검증·인가코드 교환합니다. Google은 registration은 항상 웹 `google`이며, 네이티브 id token aud는 설정 `app.security.oidc.audiences.google`로 허용합니다.", example = "ios", nullable = true) String platform,

	@Schema(description = "네이티브 SDK에서 획득한 provider access token", nullable = true) String accessToken,

	@Schema(description = "OIDC provider(google/apple)에서 획득한 id token", nullable = true) String idToken,

	@Schema(description = "OIDC 인가 코드(google/apple). 전달 시 서버가 토큰 엔드포인트로 교환해 리프레시 토큰을 저장합니다.", nullable = true) String authorizationCode,

	@Schema(description = "PKCE code_verifier(선택값). 앱이 PKCE를 사용하는 경우에만 전달하며, 미사용 환경(예: 일부 테스트/플레이그라운드)에서는 생략 가능합니다.", nullable = true) String codeVerifier) {
}
