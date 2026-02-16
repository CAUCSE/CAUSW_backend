package net.causw.app.main.domain.asset.locker.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.asset.locker.api.v2.controller.dto.response.LockerLocationResponse;
import net.causw.app.main.domain.asset.locker.api.v2.controller.dto.response.MyLockerResponse;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerLocationResult;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.MyLockerResult;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LockerResponseMapper {

	MyLockerResponse toMyLockerResponse(MyLockerResult result);

	@Mapping(target = "floor.locationName", source = "floor.locationDescription")
	LockerLocationResponse toLocationResponse(LockerLocationResult result);

	LockerLocationResponse.FloorInfo toFloorInfo(LockerLocationResult.FloorResult result);

	LockerLocationResponse.PolicyInfo toPolicyInfo(LockerLocationResult.PolicyResult result);

	LockerLocationResponse.SummaryInfo toSummaryInfo(LockerLocationResult.SummaryResult result);

	LockerLocationResponse.LockerItem toLockerItem(LockerLocationResult.LockerItemResult result);
}
