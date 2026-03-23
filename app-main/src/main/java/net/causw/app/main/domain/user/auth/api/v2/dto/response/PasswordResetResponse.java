package net.causw.app.main.domain.user.auth.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 초기화 응답 DTO")
public record PasswordResetResponse(
	@Schema(description = "초기화된 임시 비밀번호", example = "Ab1!x9Qw#2Er") String temporaryPassword) {

	public static PasswordResetResponse of(String temporaryPassword) {
		return new PasswordResetResponse(temporaryPassword);
	}
}
