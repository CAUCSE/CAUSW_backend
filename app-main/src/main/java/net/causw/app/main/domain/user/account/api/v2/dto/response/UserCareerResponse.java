package net.causw.app.main.domain.user.account.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserCareerResponse(
	@Schema(description = "사용자 경력 사항 id", example = "uuid 형식의 String 값입니다") String id,

	@Schema(description = "경력 사항 시작 년도", example = "2025") Integer startYear,

	@Schema(description = "경력 사항 시작 월", example = "3") Integer startMonth,

	@Schema(description = "경력 사항 종료 년도", example = "2026") Integer endYear,

	@Schema(description = "경력 사항 종료 월", example = "12") Integer endMonth,

	@Schema(description = "경력 사항 회사명", example = "회사명") String description) {
}