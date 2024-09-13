package net.causw.application.dto.util.dtoMapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import net.causw.adapter.persistence.calendar.Calendar;
import net.causw.application.dto.calendar.CalendarResponseDto;
import net.causw.application.dto.calendar.CalendarResponseDto.CalendarResponseDtoBuilder;
import net.causw.application.dto.calendar.CalendarsResponseDto;
import net.causw.application.dto.calendar.CalendarsResponseDto.CalendarsResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-13T05:58:22+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.12 (Azul Systems, Inc.)"
)
@Component
public class CalendarDtoMapperImpl implements CalendarDtoMapper {

    @Override
    public CalendarResponseDto toCalendarResponseDto(Calendar calendar) {
        if ( calendar == null ) {
            return null;
        }

        CalendarResponseDtoBuilder calendarResponseDto = CalendarResponseDto.builder();

        calendarResponseDto.createdAt( formatDateTime( calendar.getCreatedAt() ) );
        calendarResponseDto.updatedAt( formatDateTime( calendar.getUpdatedAt() ) );
        calendarResponseDto.image( mapUuidFileToFileUrl( calendar.getCalendarAttachImageUuidFile() ) );
        calendarResponseDto.id( calendar.getId() );
        calendarResponseDto.year( calendar.getYear() );
        calendarResponseDto.month( calendar.getMonth() );

        return calendarResponseDto.build();
    }

    @Override
    public CalendarsResponseDto toCalendarsResponseDto(Integer count, List<CalendarResponseDto> calendars) {
        if ( count == null && calendars == null ) {
            return null;
        }

        CalendarsResponseDtoBuilder calendarsResponseDto = CalendarsResponseDto.builder();

        if ( count != null ) {
            calendarsResponseDto.count( count );
        }
        if ( calendars != null ) {
            List<CalendarResponseDto> list = calendars;
            if ( list != null ) {
                calendarsResponseDto.calendars( new ArrayList<CalendarResponseDto>( list ) );
            }
        }

        return calendarsResponseDto.build();
    }
}
