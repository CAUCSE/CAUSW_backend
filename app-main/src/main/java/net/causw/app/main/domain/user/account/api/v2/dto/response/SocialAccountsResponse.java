package net.causw.app.main.domain.user.account.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 계정 연동 현황 응답")
public record SocialAccountsResponse(

	@Schema(description = "구글 계정 연동 여부", example = "true") boolean google,

	@Schema(description = "카카오 계정 연동 여부", example = "false") boolean kakao,

	@Schema(description = "애플 계정 연동 여부", example = "false") boolean apple) {
}
