package net.causw.app.main.domain.asset.locker.util;

import java.util.List;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerStatus;

public class LockerAggregator {

	public static long countAvailable(List<Locker> lockers) {
		return lockers.stream()
			.filter(l -> LockerStatus.of(l) == LockerStatus.AVAILABLE)
			.count();
	}
}
