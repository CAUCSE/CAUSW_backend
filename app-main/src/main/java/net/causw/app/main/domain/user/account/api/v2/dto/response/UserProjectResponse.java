package net.causw.app.main.domain.user.account.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserProjectResponse(
	@Schema(description = "사용자 대표 프로젝트 id (null이면 새 대표 프로젝트)", example = "uuid 형식의 String 값입니다") String id,

	@Schema(description = "대표 프로젝트 시작 년도", example = "2025") Integer startYear,

	@Schema(description = "대표 프로젝트 시작 월", example = "3") Integer startMonth,

	@Schema(description = "대표 프로젝트 종료 년도", example = "2026") Integer endYear,

	@Schema(description = "대표 프로젝트 종료 월", example = "12") Integer endMonth,

	@NotNull @Size(max = 50, message = "최대 글자 수 50을 초과했습니다.") String description) {
}
