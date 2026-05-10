package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserNicknameUpdateRequest(
	@NotBlank(message = "닉네임을 입력해 주세요.") @Size(min = 2, max = 8, message = "닉네임은 2자 이상 8자 이하여야 합니다.") @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,8}$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.") @Schema(description = "변경할 닉네임", example = "새닉네임") String nickname) {
}
