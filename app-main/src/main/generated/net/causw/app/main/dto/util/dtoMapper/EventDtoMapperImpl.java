package net.causw.app.main.dto.util.dtoMapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.event.Event;
import net.causw.app.main.dto.event.EventResponseDto;
import net.causw.app.main.dto.event.EventResponseDto.EventResponseDtoBuilder;
import net.causw.app.main.dto.event.EventsResponseDto;
import net.causw.app.main.dto.event.EventsResponseDto.EventsResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class EventDtoMapperImpl implements EventDtoMapper {

    @Override
    public EventResponseDto toEventResponseDto(Event event) {
        if ( event == null ) {
            return null;
        }

        EventResponseDtoBuilder eventResponseDto = EventResponseDto.builder();

        eventResponseDto.createdAt( formatDateTime( event.getCreatedAt() ) );
        eventResponseDto.updatedAt( formatDateTime( event.getUpdatedAt() ) );
        eventResponseDto.image( mapUuidFileToFileUrl( event.getEventAttachImage() ) );
        eventResponseDto.id( event.getId() );
        eventResponseDto.url( event.getUrl() );
        eventResponseDto.isDeleted( event.getIsDeleted() );

        return eventResponseDto.build();
    }

    @Override
    public EventsResponseDto toEventsResponseDto(Integer count, List<EventResponseDto> events) {
        if ( count == null && events == null ) {
            return null;
        }

        EventsResponseDtoBuilder eventsResponseDto = EventsResponseDto.builder();

        if ( count != null ) {
            eventsResponseDto.count( count );
        }
        if ( events != null ) {
            List<EventResponseDto> list = events;
            if ( list != null ) {
                eventsResponseDto.events( new ArrayList<EventResponseDto>( list ) );
            }
        }

        return eventsResponseDto.build();
    }
}
