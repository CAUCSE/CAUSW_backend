package net.causw.app.main.domain.asset.locker.service.v2.dto;

import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.enums.LockerLogAction;

public record LockerLogListCondition(
	String userKeyword,
	LockerLogAction action,
	LockerName lockerLocationName,
	Long lockerNumber) {
}
