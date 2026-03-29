package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserPasswordUpdateRequest(
	@NotBlank(message = "현재 비밀번호를 입력해 주세요.") @Schema(description = "현재 비밀번호", example = "Current1!") String currentPassword,

	@NotBlank(message = "새 비밀번호를 입력해 주세요.") @Schema(description = "새 비밀번호 (영문, 숫자, 특수문자 포함 8자 이상 20자 이하)", example = "NewPass1!") String newPassword,
	@NotBlank(message = "새 비밀번호 확인을 입력해 주세요.") @Schema(description = "새 비밀번호 확인", example = "NewPass1!") String newPasswordConfirm) {
}
