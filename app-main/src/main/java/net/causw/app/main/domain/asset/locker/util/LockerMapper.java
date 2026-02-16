package net.causw.app.main.domain.asset.locker.util;

import java.util.List;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.entity.LockerStatus;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerLocationResult;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.MyLockerResult;

public class LockerMapper {

	public static MyLockerResult toMyLockerResult(Locker locker) {
		LockerLocation location = locker.getLocation();
		String displayName = location.getDescription() + " " + locker.getLockerNumber() + "번";
		return MyLockerResult.of(locker.getId(), displayName, locker.getExpireDate());
	}

	public static List<LockerLocationResult.LockerItemResult> toLockerItemResults(
		List<Locker> lockers, String userId) {
		return lockers.stream()
			.map(locker -> LockerLocationResult.LockerItemResult.builder()
				.lockerId(locker.getId())
				.number(String.valueOf(locker.getLockerNumber()))
				.status(LockerStatus.of(locker, userId))
				.build())
			.toList();
	}

	public static LockerLocationResult toLocationResult(
		LockerLocation location,
		List<Locker> lockers,
		List<LockerLocationResult.LockerItemResult> lockerItems,
		boolean canApplyPolicy,
		boolean canExtendPolicy) {

		return LockerLocationResult.builder()
			.floor(LockerLocationResult.FloorResult.builder()
				.locationId(location.getId())
				.locationName(location.getName())
				.locationDescription(location.getDescription())
				.build())
			.currentPolicy(LockerLocationResult.PolicyResult.builder()
				.canApply(canApplyPolicy)
				.canExtend(canExtendPolicy)
				.build())
			.summary(LockerLocationResult.SummaryResult.builder()
				.totalCount(lockers.size())
				.availableCount(LockerAggregator.countAvailable(lockers))
				.build())
			.lockers(lockerItems)
			.build();
	}
}
