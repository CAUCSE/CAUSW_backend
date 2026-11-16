package net.causw.app.main.domain.campus.event.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.event.entity.Event;
import net.causw.app.main.domain.campus.event.service.dto.EventCreateCommand;
import net.causw.app.main.domain.campus.event.service.dto.EventOrderUpdateCommand;
import net.causw.app.main.domain.campus.event.service.dto.EventResult;
import net.causw.app.main.domain.campus.event.service.dto.EventUpdateCommand;
import net.causw.app.main.domain.campus.event.service.implementation.EventReader;
import net.causw.app.main.domain.campus.event.service.implementation.EventWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

	private final EventReader eventReader;
	private final EventWriter eventWriter;

	@Transactional(readOnly = true)
	public List<EventResult> getActiveEvents() {
		return eventReader.findAllActive().stream()
			.map(EventResult::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<EventResult> getAllEvents() {
		return eventReader.findAll().stream()
			.map(EventResult::from)
			.toList();
	}

	@Transactional
	public EventResult createEvent(EventCreateCommand command) {
		Event event = eventWriter.create(Event.of(
			command.title(), command.landingUrl(), command.isActive(), eventReader.getNextDisplayOrder()));
		return EventResult.from(event);
	}

	@Transactional
	public EventResult updateEvent(String eventId, EventUpdateCommand command) {
		Event event = eventReader.getById(eventId);
		eventWriter.update(event, command.title(), command.landingUrl(), command.isActive());
		return EventResult.from(event);
	}

	@Transactional
	public void updateEventOrder(EventOrderUpdateCommand command) {
		List<Event> events = eventReader.findAllByIds(command.eventIds());
		eventWriter.updateDisplayOrders(events, command.eventIds());
	}

	@Transactional
	public void deleteEvent(String eventId) {
		Event event = eventReader.getById(eventId);
		eventWriter.softDelete(event);
	}
}
