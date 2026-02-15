package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerListRequest;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerLogListRequest;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response.LockerListItemResponse;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response.LockerLogListItemResponse;
import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLog;
import net.causw.app.main.domain.asset.locker.entity.LockerStatus;
import net.causw.app.main.domain.asset.locker.service.v2.dto.LockerListCondition;
import net.causw.app.main.domain.asset.locker.service.v2.dto.LockerLogListCondition;

@Mapper(componentModel = "spring", imports = LockerStatus.class)
public interface LockerListMapper {

	LockerListCondition toCondition(LockerListRequest request);

	@Mapping(target = "location", expression = "java(locker.getLocation().getDescription())")
	@Mapping(target = "status", expression = "java(LockerStatus.of(locker))")
	@Mapping(target = "user", expression = "java(locker.getUser().map(u -> u.getName() + \"(\" + u.getStudentId() + \")\").orElse(null))")
	LockerListItemResponse toResponse(Locker locker);

	LockerLogListCondition toCondition(LockerLogListRequest request);

	LockerLogListItemResponse toResponse(LockerLog lockerLog);
}
