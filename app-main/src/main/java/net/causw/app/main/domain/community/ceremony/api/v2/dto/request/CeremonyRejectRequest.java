package net.causw.app.main.domain.community.ceremony.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "경조사 거절 요청")
public record CeremonyRejectRequest(

	@Schema(description = "거절 사유", example = "경조사 신청 요건에 부합하지 않습니다.") @Size(max = 250, message = "경조사 거절 사유는 최대 250자까지 입력 가능합니다.") @NotBlank(message = "거절 사유는 필수입니다.") String rejectReason) {
}
