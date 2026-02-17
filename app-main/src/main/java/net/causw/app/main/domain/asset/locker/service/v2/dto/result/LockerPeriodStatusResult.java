package net.causw.app.main.domain.asset.locker.service.v2.dto.result;

import java.time.LocalDateTime;

import net.causw.app.main.domain.asset.locker.enums.LockerPeriodPhase;

import lombok.Builder;

@Builder
public record LockerPeriodStatusResult(
	LockerPeriodPhase phase,
	LocalDateTime startAt,
	LocalDateTime endAt) {
}
