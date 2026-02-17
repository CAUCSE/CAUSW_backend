package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.util.List;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerStatus;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerFloorListResult;

public class LockerAggregator {

	public static long countAvailable(List<Locker> lockers) {
		return lockers.stream()
			.filter(l -> l.getStatus() == LockerStatus.AVAILABLE)
			.count();
	}

	public static long sumTotalCount(List<LockerFloorListResult.FloorItemResult> floorItems) {
		return floorItems.stream()
			.mapToLong(LockerFloorListResult.FloorItemResult::totalCount)
			.sum();
	}

	public static long sumAvailableCount(List<LockerFloorListResult.FloorItemResult> floorItems) {
		return floorItems.stream()
			.mapToLong(LockerFloorListResult.FloorItemResult::availableCount)
			.sum();
	}
}
