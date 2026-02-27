package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserNicknameUpdateRequest(
	@NotBlank(message = "닉네임을 입력해 주세요.")
	@Size(max = 8, message = "닉네임은 8자 이하로 입력해 주세요.")
	@Schema(description = "변경할 닉네임", example = "새닉네임")
	String nickname) {
}
