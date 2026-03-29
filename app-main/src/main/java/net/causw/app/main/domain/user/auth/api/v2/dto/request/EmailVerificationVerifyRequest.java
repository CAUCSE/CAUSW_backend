package net.causw.app.main.domain.user.auth.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증 코드 검증 요청 DTO")
public record EmailVerificationVerifyRequest(
	@Email(message = "이메일 형식에 맞지 않습니다.") @NotBlank(message = "이메일을 입력해 주세요.") @Schema(description = "인증할 이메일", example = "user@cau.ac.kr") String email,

	@NotBlank(message = "인증 코드를 입력해 주세요.") @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.") @Schema(description = "6자리 숫자 인증 코드", example = "123456") String verificationCode) {
}
