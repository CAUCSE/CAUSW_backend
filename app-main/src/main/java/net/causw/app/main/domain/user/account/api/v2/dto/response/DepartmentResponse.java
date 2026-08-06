package net.causw.app.main.domain.user.account.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record DepartmentResponse(
	@Schema(description = "학과 코드", example = "SCHOOL_OF_SW") String code,
	@Schema(description = "학과 이름", example = "소프트웨어학부") String name) {
}
