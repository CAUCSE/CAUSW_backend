package net.causw.app.main.domain.asset.locker.service.v2.dto.result;

import java.time.LocalDateTime;

import net.causw.app.main.domain.asset.locker.enums.LockerPeriodPhase;

import lombok.Builder;

/**
 * 현재 사물함 신청/연장 기간 상태를 나타내는 DTO.
 *
 * @param phase   현재 사물함 기간 단계(예: 신청 기간, 연장 기간, 비활성 등)
 * @param startAt 해당 기간 시작 일시
 * @param endAt   해당 기간 종료 일시
 */
@Builder
public record LockerPeriodStatusResult(
	LockerPeriodPhase phase,
	LocalDateTime startAt,
	LocalDateTime endAt) {
}
