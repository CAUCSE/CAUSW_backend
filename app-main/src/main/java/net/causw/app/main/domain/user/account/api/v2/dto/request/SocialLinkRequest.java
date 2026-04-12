package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "소셜 계정 연동 요청")
public record SocialLinkRequest(

	@NotBlank(message = "provider를 입력해 주세요.") @Schema(description = "소셜 provider (kakao, google, apple)", example = "kakao") String provider,

	@Schema(description = "provider access token - 카카오 연동 시 필수, 구글/애플은 null", nullable = true) String accessToken,

	@Schema(description = "provider id token - 구글/애플 연동 시 필수, 카카오는 null", nullable = true) String idToken) {
}
