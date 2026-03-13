package net.causw.app.main.domain.user.auth.api.v2.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 계정 정보")
public record FindEmailSocialAccountResponse(
	@Schema(description = "소셜 로그인 제공자", example = "KAKAO") String provider,
	@Schema(description = "소셜 계정 연동일", example = "2024-01-01") LocalDate createdAt) {
}
