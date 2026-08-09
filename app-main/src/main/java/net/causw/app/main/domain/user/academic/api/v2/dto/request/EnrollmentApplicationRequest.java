package net.causw.app.main.domain.user.academic.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record EnrollmentApplicationRequest(
	@Schema(description = "유저 작성 특이사항", requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Size(max = 500, message = "특이사항은 500자 이하여야 합니다.") String note) {
}
