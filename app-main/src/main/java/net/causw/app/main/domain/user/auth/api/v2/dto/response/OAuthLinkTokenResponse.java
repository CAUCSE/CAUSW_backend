package net.causw.app.main.domain.user.auth.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 계정 연동 OAuth 초기화 응답")
public record OAuthLinkTokenResponse(
	@Schema(description = "1회용 링크 토큰. /oauth2/authorization/{provider}?linkToken={value} 로 전달합니다.") String linkToken) {
}
