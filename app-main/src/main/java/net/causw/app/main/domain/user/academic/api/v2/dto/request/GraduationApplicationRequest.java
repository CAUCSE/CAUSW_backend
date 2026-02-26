package net.causw.app.main.domain.user.academic.api.v2.dto.request;

import net.causw.app.main.domain.user.account.enums.user.GraduationType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GraduationApplicationRequest(
	@Schema(description = "졸업년도", example = "2026", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "졸업년도는 필수입니다.") @Positive(message = "졸업년도는 1 이상이어야 합니다.") Integer graduationYear,
	@Schema(description = "졸업 월 (FEBRUARY, AUGUST)", example = "AUGUST", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "졸업 유형은 필수입니다.") GraduationType graduationType,
	@Schema(description = "유저 작성 특이사항", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String note) {
}
