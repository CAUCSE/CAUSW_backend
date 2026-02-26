package net.causw.app.main.domain.user.academic.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EnrollmentApplicationRequest(
	@Schema(description = "본 학기 기준 등록 완료 학기 차수", example = "5", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "등록 완료 학기 차수는 필수입니다.") @Positive(message = "등록 완료 학기 차수는 1 이상이어야 합니다.") Integer currentCompletedSemester,
	@Schema(description = "유저 작성 특이사항", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String note) {
}
