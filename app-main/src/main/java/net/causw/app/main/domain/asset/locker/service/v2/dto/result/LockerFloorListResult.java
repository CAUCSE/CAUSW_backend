package net.causw.app.main.domain.asset.locker.service.v2.dto.result;

import java.util.List;

import lombok.Builder;

@Builder
public record LockerFloorListResult(
	SummaryResult summary,
	List<FloorItemResult> floors) {

	@Builder
	public record SummaryResult(long totalCount, long availableCount) {
	}

	@Builder
	public record FloorItemResult(
		String locationId,
		String floorName,
		long totalCount,
		long availableCount) {
	}
}
