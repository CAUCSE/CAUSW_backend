package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.calendar.Calendar;
import net.causw.application.dto.calendar.CalendarResponseDto;
import net.causw.application.dto.calendar.CalendarsResponseDto;
import net.causw.application.dto.util.dtoMapper.custom.FormatDateTimeDtoMapper;
import net.causw.application.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CalendarDtoMapper extends FormatDateTimeDtoMapper, UuidFileToUrlDtoMapper {

    CalendarDtoMapper INSTANCE = Mappers.getMapper(CalendarDtoMapper.class);

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "image", source = "calendar.calendarAttachImageUuidFile", qualifiedByName = "mapUuidFileToFileUrl")
    CalendarResponseDto toCalendarResponseDto(Calendar calendar);

    @Mapping(target = "calendars", source = "calendars")
    CalendarsResponseDto toCalendarsResponseDto(Integer count, List<CalendarResponseDto> calendars);


}
