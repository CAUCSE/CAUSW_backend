package net.causw.application.event;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.event.Event;
import net.causw.adapter.persistence.repository.EventRepository;
import net.causw.application.dto.event.EventCreateRequestDto;
import net.causw.application.dto.event.EventResponseDto;
import net.causw.application.dto.event.EventUpdateRequestDto;
import net.causw.application.dto.event.EventsResponseDto;
import net.causw.application.dto.util.DtoMapper;
import net.causw.application.storage.StorageService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public EventsResponseDto findEvents() {
        List<EventResponseDto> events = eventRepository.findByIsDeletedIsFalse().stream()
                .map(DtoMapper.INSTANCE::toEventResponseDto)
                .toList();

        return DtoMapper.INSTANCE.toEventsResponseDto(
                events.size(),
                events
        );
    }

    @Transactional
    public EventResponseDto createEvent(EventCreateRequestDto eventCreateRequestDto) {
        if (eventRepository.findByIsDeletedIsFalse().size() >= 10) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.EVENT_MAX_CREATED
            );
        }

        return DtoMapper.INSTANCE.toEventResponseDto(
                eventRepository.save(
                        Event.of(
                                eventCreateRequestDto.getUrl(),
                                storageService.uploadFile(eventCreateRequestDto.getImage(), "EVENT"),
                                false
                        )
                )
        );
    }

    @Transactional
    public EventResponseDto updateEvent(String eventId, EventUpdateRequestDto eventUpdateRequestDto) {
        Event event = getEvent(eventId);
        event.update(
                eventUpdateRequestDto.getUrl(),
                storageService.uploadFile(eventUpdateRequestDto.getImage(), "EVENT")
        );
        return DtoMapper.INSTANCE.toEventResponseDto(eventRepository.save(event));
    }

    @Transactional
    public EventResponseDto deleteEvent(String eventId) {
        Event event = getEvent(eventId);
        event.setIsDeleted(true);
        return DtoMapper.INSTANCE.toEventResponseDto(eventRepository.save(event));
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
