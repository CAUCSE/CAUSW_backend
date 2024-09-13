package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.event.Event;
import net.causw.application.dto.event.EventResponseDto;
import net.causw.application.dto.event.EventsResponseDto;
import net.causw.application.dto.util.dtoMapper.custom.FormatDateTimeDtoMapper;
import net.causw.application.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventDtoMapper extends FormatDateTimeDtoMapper, UuidFileToUrlDtoMapper {

    EventDtoMapper INSTANCE = Mappers.getMapper(EventDtoMapper.class);


    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "image", source = "event.eventImageUuidFile", qualifiedByName = "mapUuidFileToFileUrl")
    EventResponseDto toEventResponseDto(Event event);

    @Mapping(target = "events", source = "events")
    EventsResponseDto toEventsResponseDto(Integer count, List<EventResponseDto> events);

}
