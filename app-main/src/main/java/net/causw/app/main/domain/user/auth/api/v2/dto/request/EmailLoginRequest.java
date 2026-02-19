package net.causw.app.main.domain.user.auth.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "이메일 로그인 요청 dto")
public record EmailLoginRequest(
	@Email(message = "이메일 형식에 맞지 않습니다.") @NotBlank(message = "이메일을 입력해 주세요.") @Schema(description = "이메일", example = "yebin@cau.ac.kr") String email,

	@NotBlank(message = "비밀번호를 입력해 주세요.") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()-_?]).{8,20}$", message = "비밀번호는 8자 이상 20자 이하이며, 영문, 숫자, 특수문자가 각 1개 이상 포함되어야 합니다.") @Schema(description = "비밀번호", example = "password00!!") String password) {
}
