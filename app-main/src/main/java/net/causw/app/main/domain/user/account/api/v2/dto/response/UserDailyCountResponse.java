package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일일 신규 가입자 수 응답")
public record UserDailyCountResponse(
	@Schema(description = "조회 기준 날짜", example = "2026-03-28") LocalDate targetDate,

	@Schema(description = "해당 날짜의 신규 가입자 수", example = "42") Long count) {
}
