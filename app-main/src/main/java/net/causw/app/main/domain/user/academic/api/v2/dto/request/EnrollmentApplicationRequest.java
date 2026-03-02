package net.causw.app.main.domain.user.academic.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record EnrollmentApplicationRequest(
	@Schema(description = "유저 작성 특이사항", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String note) {
}
