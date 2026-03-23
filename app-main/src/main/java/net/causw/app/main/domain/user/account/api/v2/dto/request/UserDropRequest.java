package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 추방 요청")
public record UserDropRequest(

	@Schema(description = "추방 사유", example = "운영 정책 위반") @NotBlank(message = "추방 사유를 입력해 주세요.") String dropReason) {
}
