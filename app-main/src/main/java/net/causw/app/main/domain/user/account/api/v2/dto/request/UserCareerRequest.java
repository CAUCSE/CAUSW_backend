package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCareerRequest(
	@Schema(description = "사용자 경력 사항 id (null이면 새 경력 사항)", example = "uuid 형식의 String 값입니다") String id,

	@NotNull @Min(value = 1900) @Schema(description = "경력 사항 시작 년도", example = "2025") Integer startYear,

	@NotNull @Min(value = 1) @Max(value = 12) @Schema(description = "경력 사항 시작 월", example = "3") Integer startMonth,

	@Min(value = 1900) @Schema(description = "경력 사항 종료 년도", example = "2026") Integer endYear,

	@Min(value = 1) @Max(value = 12) @Schema(description = "경력 사항 종료 월", example = "12") Integer endMonth,

	@NotBlank(message = "경력 사항 회사명을 입력해야 합니다.") @Size(max = 50, message = "경력 사항은 최대 50자까지 입력 가능합니다.") @Schema(description = "경력 사항 회사명", example = "회사명") String description) {
}
