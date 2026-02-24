package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRegistrationRequest(
	@NotBlank(message = "이름을 입력해 주세요.") @Schema(description = "이름 (본명)", example = "홍길동") String name,

	@NotBlank(message = "연락처를 입력해 주세요.") @Schema(description = "연락처", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED) @Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식에 맞지 않습니다.") String phoneNumber,

	@NotBlank(message = "닉네임을 입력해 주세요.") @Schema(description = "닉네임", example = "푸앙") String nickname) {
}
