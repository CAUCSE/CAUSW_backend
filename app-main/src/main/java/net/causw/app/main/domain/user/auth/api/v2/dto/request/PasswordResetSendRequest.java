package net.causw.app.main.domain.user.auth.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 초기화 인증번호 발송 요청 DTO")
public record PasswordResetSendRequest(
	@NotBlank(message = "이름을 입력해 주세요.") @Schema(description = "사용자 이름", example = "홍길동") String name,

	@Email(message = "이메일 형식에 맞지 않습니다.") @NotBlank(message = "이메일을 입력해 주세요.") @Schema(description = "인증번호를 받을 이메일", example = "user@cau.ac.kr") String email) {
}
