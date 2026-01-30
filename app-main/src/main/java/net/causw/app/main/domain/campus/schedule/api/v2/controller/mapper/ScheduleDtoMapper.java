package net.causw.app.main.domain.campus.schedule.api.v2.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.campus.schedule.api.v2.controller.dto.request.ScheduleRequest;
import net.causw.app.main.domain.campus.schedule.api.v2.controller.dto.response.ScheduleResponse;
import net.causw.app.main.domain.campus.schedule.service.v2.dto.ScheduleDto;
import net.causw.app.main.domain.user.account.entity.user.User;

@Mapper(componentModel = "spring")
public interface ScheduleDtoMapper {

	ScheduleDtoMapper INSTANCE = Mappers.getMapper(ScheduleDtoMapper.class);

	@Mapping(target = "creator", source = "user")
	ScheduleDto toScheduleDto(ScheduleRequest request, User user);

	ScheduleDto toScheduleDto(ScheduleRequest request);

	ScheduleResponse toScheduleResponse(ScheduleDto dto);
}
