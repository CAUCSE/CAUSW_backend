package net.causw.app.main.domain.campus.event.api.v1.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.campus.event.api.v1.dto.EventResponseDto;
import net.causw.app.main.domain.campus.event.api.v1.dto.EventsResponseDto;
import net.causw.app.main.shared.dto.util.dtoMapper.custom.FormatDateTimeDtoMapper;
import net.causw.app.main.shared.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;
import net.causw.app.main.domain.campus.event.entity.Event;

@Mapper(componentModel = "spring")
public interface EventDtoMapper extends FormatDateTimeDtoMapper, UuidFileToUrlDtoMapper {

	EventDtoMapper INSTANCE = Mappers.getMapper(EventDtoMapper.class);

	@Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "formatDateTime")
	@Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "formatDateTime")
	@Mapping(target = "image", source = "event.eventAttachImage", qualifiedByName = "mapUuidFileToFileUrl")
	EventResponseDto toEventResponseDto(Event event);

	@Mapping(target = "events", source = "events")
	EventsResponseDto toEventsResponseDto(Integer count, List<EventResponseDto> events);

}
