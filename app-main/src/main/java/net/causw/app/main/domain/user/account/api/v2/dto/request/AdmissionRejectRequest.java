package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "재학인증 신청 거절 요청")
public record AdmissionRejectRequest(

	@Schema(description = "거절 사유", example = "증빙서류가 불명확합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "거절 사유를 입력해 주세요.")
	String rejectReason) {
}
