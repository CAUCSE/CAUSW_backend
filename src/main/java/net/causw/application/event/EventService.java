package net.causw.application.event;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.event.Event;
import net.causw.adapter.persistence.repository.EventRepository;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.event.EventCreateRequestDto;
import net.causw.application.dto.event.EventResponseDto;
import net.causw.application.dto.event.EventUpdateRequestDto;
import net.causw.application.dto.event.EventsResponseDto;
import net.causw.application.dto.util.dtoMapper.EventDtoMapper;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.FilePath;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UuidFileService uuidFileService;

    @Transactional(readOnly = true)
    public EventsResponseDto findEvents() {
        List<EventResponseDto> events = eventRepository.findByIsDeletedIsFalse().stream()
                .map(EventDtoMapper.INSTANCE::toEventResponseDto)
                .toList();

        return EventDtoMapper.INSTANCE.toEventsResponseDto(
                events.size(),
                events
        );
    }

    @Transactional
    public EventResponseDto createEvent(EventCreateRequestDto eventCreateRequestDto) {
        if (eventRepository.findByIsDeletedIsFalse().size() >= StaticValue.MAX_NUM_EVENT) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.EVENT_MAX_CREATED
            );
        }

        UuidFile uuidFile = uuidFileService.saveFile(eventCreateRequestDto.getImage(), FilePath.EVENT);

        return EventDtoMapper.INSTANCE.toEventResponseDto(
                eventRepository.save(
                        Event.of(
                                eventCreateRequestDto.getUrl(),
                                uuidFile,
                                false
                        )
                )
        );
    }

    @Transactional
    public EventResponseDto updateEvent(String eventId, EventUpdateRequestDto eventUpdateRequestDto) {
        Event event = getEvent(eventId);
        UuidFile uuidFile = uuidFileService.saveFile(eventUpdateRequestDto.getImage(), FilePath.EVENT);
        event.update(
                eventUpdateRequestDto.getUrl(),
                uuidFile
        );
        return EventDtoMapper.INSTANCE.toEventResponseDto(eventRepository.save(event));
    }

    @Transactional
    public EventResponseDto deleteEvent(String eventId) {
        Event event = getEvent(eventId);
        event.setIsDeleted(true);
        return EventDtoMapper.INSTANCE.toEventResponseDto(eventRepository.save(event));
    }

    private Event getEvent(String eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.EVENT_NOT_FOUND
                )
        );
    }

}
