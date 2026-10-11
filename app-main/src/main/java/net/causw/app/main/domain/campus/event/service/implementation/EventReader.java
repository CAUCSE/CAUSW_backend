package net.causw.app.main.domain.campus.event.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.campus.event.entity.Event;
import net.causw.app.main.domain.campus.event.repository.EventRepository;
import net.causw.app.main.shared.exception.errorcode.EventErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventReader {

	private final EventRepository eventRepository;

	public Event getById(String id) {
		return eventRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(EventErrorCode.EVENT_NOT_FOUND::toBaseException);
	}

	public List<Event> findAllActive() {
		return eventRepository.findAllByIsActiveTrueAndDeletedAtIsNullOrderByDisplayOrderAscCreatedAtDesc();
	}

	public List<Event> findAll() {
		return eventRepository.findAllByDeletedAtIsNullOrderByDisplayOrderAscCreatedAtDesc();
	}
}
