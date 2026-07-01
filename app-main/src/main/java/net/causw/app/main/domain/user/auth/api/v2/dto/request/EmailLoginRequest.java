package net.causw.app.main.domain.user.auth.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 로그인 요청 dto")
public record EmailLoginRequest(
	@Email(message = "이메일 형식에 맞지 않습니다.") @NotBlank(message = "이메일을 입력해 주세요.") @Schema(description = "이메일", example = "user@cau.ac.kr") String email,

	@NotBlank(message = "비밀번호를 입력해 주세요.") @Schema(description = "비밀번호", example = "abcdefg12!") String password) {
}
