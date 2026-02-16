package net.causw.app.main.domain.asset.locker.service.v2.dto.result;

import java.util.List;

import net.causw.app.main.domain.asset.locker.entity.LockerStatus;

import lombok.Builder;

@Builder
public record LockerLocationResult(
	FloorResult floor,
	PolicyResult currentPolicy,
	SummaryResult summary,
	List<LockerItemResult> lockers,
	ActionResult actions) {

	@Builder
	public record FloorResult(String locationId, String locationName) {
	}

	@Builder
	public record PolicyResult(boolean canApply, boolean canExtend) {
	}

	@Builder
	public record SummaryResult(long totalCount, long availableCount) {
	}

	@Builder
	public record LockerItemResult(String lockerId, String number, LockerStatus status) {
	}

	@Builder
	public record ActionResult(boolean canApply, boolean canReturn, boolean canExtend) {
	}
}
