package net.causw.app.main.dto.util.dtoMapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.model.entity.event.Event;
import net.causw.app.main.dto.event.EventResponseDto;
import net.causw.app.main.dto.event.EventsResponseDto;
import net.causw.app.main.dto.util.dtoMapper.custom.FormatDateTimeDtoMapper;
import net.causw.app.main.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;

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
