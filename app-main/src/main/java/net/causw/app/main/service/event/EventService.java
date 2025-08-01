package net.causw.app.main.service.event;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.model.entity.event.Event;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.EventAttachImage;
import net.causw.app.main.domain.model.enums.uuidFile.FilePath;
import net.causw.app.main.dto.event.EventCreateRequestDto;
import net.causw.app.main.dto.event.EventResponseDto;
import net.causw.app.main.dto.event.EventUpdateRequestDto;
import net.causw.app.main.dto.event.EventsResponseDto;
import net.causw.app.main.dto.util.dtoMapper.EventDtoMapper;
import net.causw.app.main.infrastructure.aop.annotation.MeasureTime;
import net.causw.app.main.repository.event.EventRepository;
import net.causw.app.main.service.uuidFile.UuidFileService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

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
	public EventResponseDto updateEvent(String eventId, EventUpdateRequestDto eventUpdateRequestDto,
		MultipartFile eventImage) {
		Event event = getEvent(eventId);

		// 이미지가 없을 경우 기존 이미지를 그대로 사용, 이미지가 있을 경우 새로운 이미지로 교체 (event의 이미지는 not null임)
		if (!(eventImage == null || eventImage.isEmpty())) {
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
