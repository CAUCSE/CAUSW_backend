package net.causw.app.main.domain.user.auth.api.v2.dto.request;

import java.util.List;

import net.causw.app.main.domain.user.account.policy.PasswordPolicy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 회원가입 요청 dto")
public record EmailSignupRequest(
	@Email(message = "이메일 형식에 맞지 않습니다.") @NotBlank(message = "이메일을 입력해 주세요.") @Schema(description = "이메일", example = "user@cau.ac.kr") String email,

	@NotBlank(message = "비밀번호를 입력해 주세요.") @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.VALIDATION_MESSAGE) @Schema(description = "비밀번호", example = "abcdefg12!") String password,

	@NotBlank(message = "이름을 입력해 주세요.") @Size(max = 20, message = "이름은 20자 이하로 입력해 주세요.") @Schema(description = "이름 (본명)", example = "홍길동") String name,

	@NotBlank(message = "연락처를 입력해 주세요.") @Schema(description = "연락처", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED) @Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식에 맞지 않습니다.") String phoneNumber,

	@NotBlank(message = "닉네임을 입력해 주세요.") @Size(min = 2, max = 8, message = "닉네임은 2자 이상 8자 이하여야 합니다.") @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,8}$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.") @Schema(description = "닉네임", example = "푸앙") String nickname,

	@NotBlank(message = "인증번호를 입력해 주세요.") @Schema(description = "인증확인한 인증번호", example = "1234AB") String emailVerificationCode,

	@NotEmpty(message = "동의할 약관 ID를 입력해 주세요.") @Schema(description = "동의한 약관 ID 목록 (타입별 최신 필수 약관 ID 포함)") List<String> agreedTermsIds) {
}
