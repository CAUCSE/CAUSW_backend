package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserNicknameUpdateRequest(
	@NotBlank(message = "닉네임을 입력해 주세요.") @Schema(description = "변경할 닉네임", example = "새닉네임") String nickname) {
}
