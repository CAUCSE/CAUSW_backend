package net.causw.app.main.domain.asset.locker.api.v2.controller.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.asset.locker.enums.LockerPeriodPhase;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사물함 현재 기간 정책 응답")
public record LockerPeriodStatusResponse(
	@Schema(description = "현재 기간 상태", example = "APPLY") LockerPeriodPhase phase,
	@Schema(description = "기간 시작 일시", example = "2026-03-07T23:59:59") LocalDateTime startAt,
	@Schema(description = "기간 종료 일시", example = "2026-03-10T23:59:50") LocalDateTime endAt) {
}
