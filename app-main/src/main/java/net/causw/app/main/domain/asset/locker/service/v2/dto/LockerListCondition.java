package net.causw.app.main.domain.asset.locker.service.v2.dto;

import net.causw.app.main.domain.asset.locker.entity.LockerName;

public record LockerListCondition(
	LockerName location,
	Boolean isActive,
	Boolean isOccupied) {
}
