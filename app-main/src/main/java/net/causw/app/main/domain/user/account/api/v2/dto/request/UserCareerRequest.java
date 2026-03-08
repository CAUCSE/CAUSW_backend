package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCareerRequest(
	@Schema(description = "사용자 경력 사항 id (null이면 새 경력 사항)", example = "uuid 형식의 String 값입니다") String id,

	@Schema(description = "경력 사항 시작 년도", example = "2025") Integer startYear,

	@Schema(description = "경력 사항 시작 월", example = "3") Integer startMonth,

	@Schema(description = "경력 사항 종료 년도", example = "2026") Integer endYear,

	@Schema(description = "경력 사항 종료 월", example = "12") Integer endMonth,

	@NotNull @Size(max = 50, message = "최대 글자 수 50을 초과했습니다.") @Schema(description = "경력 사항 회사명", example = "회사명") String description) {
}
