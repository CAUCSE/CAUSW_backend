package net.causw.app.main.domain.user.auth.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "네이티브 소셜 로그인 요청 DTO")
public record SocialNativeLoginRequest(
	@NotBlank(message = "소셜 로그인 provider를 입력해 주세요.") @Schema(description = "소셜 로그인 provider", example = "google") String provider,

	@Schema(description = "네이티브 SDK에서 획득한 provider access token", nullable = true) String accessToken,

	@Schema(description = "OIDC provider(google/apple)에서 획득한 id token", nullable = true) String idToken) {
}
