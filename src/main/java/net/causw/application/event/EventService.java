package net.causw.application.event;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.event.Event;
import net.causw.adapter.persistence.repository.event.EventRepository;
import net.causw.adapter.persistence.uuidFile.joinEntity.EventAttachImage;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.event.EventCreateRequestDto;
import net.causw.application.dto.event.EventResponseDto;
import net.causw.application.dto.event.EventUpdateRequestDto;
import net.causw.application.dto.event.EventsResponseDto;
import net.causw.application.dto.util.dtoMapper.EventDtoMapper;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.uuidFile.FilePath;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@MeasureTime
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UuidFileService uuidFileService;

    @Transactional(readOnly = true)
    public EventsResponseDto findEvents() {
        List<EventResponseDto> events = eventRepository.findByIsDeletedIsFalseOrderByCreatedAtDesc().stream()
                .map(EventDtoMapper.INSTANCE::toEventResponseDto)
                .toList();

        return EventDtoMapper.INSTANCE.toEventsResponseDto(
                events.size(),
                events
        );
    }

    @Transactional
    public EventResponseDto createEvent(EventCreateRequestDto eventCreateRequestDto, MultipartFile eventImage) {
        if (eventRepository.findByIsDeletedIsFalseOrderByCreatedAtDesc().size() >= StaticValue.MAX_NUM_EVENT) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.EVENT_MAX_CREATED
            );
        }

        UuidFile uuidFile = uuidFileService.saveFile(eventImage, FilePath.EVENT);

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
    public EventResponseDto updateEvent(String eventId, EventUpdateRequestDto eventUpdateRequestDto, MultipartFile eventImage) {
        Event event = getEvent(eventId);

        // 이미지가 없을 경우 기존 이미지를 그대로 사용, 이미지가 있을 경우 새로운 이미지로 교체 (event의 이미지는 not null임)
        if ( !(eventImage == null || eventImage.isEmpty()) ) {
            event.getEventAttachImage().setUuidFile(
                    uuidFileService.updateFile(
                            event.getEventAttachImage().getUuidFile(),
                            eventImage,
                            FilePath.EVENT
                    )
            );
        }

        EventAttachImage eventAttachImage = event.getEventAttachImage();

        event.update(
                eventUpdateRequestDto.getUrl(),
                eventAttachImage
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
