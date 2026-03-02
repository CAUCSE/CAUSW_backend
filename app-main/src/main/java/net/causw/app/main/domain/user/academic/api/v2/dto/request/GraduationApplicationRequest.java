package net.causw.app.main.domain.user.academic.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GraduationApplicationRequest(
	@Schema(description = "졸업년도", example = "2026", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "졸업년도는 필수입니다.") @Min(value = 1000, message = "올바른 네 자리 연도를 입력해 주세요.") @Max(value = 9999, message = "올바른 네 자리 연도를 입력해 주세요.") Integer graduationYear,
	@Schema(description = "유저 작성 특이사항", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String note) {
}
