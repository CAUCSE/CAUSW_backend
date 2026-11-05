package net.causw.app.main.domain.campus.event.service.implementation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.campus.event.entity.Event;
import net.causw.app.main.domain.campus.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventWriter {

	private final EventRepository eventRepository;

	public Event create(Event event) {
		return eventRepository.save(event);
	}

	public void update(Event event, String title, String landingUrl, Boolean isActive) {
		event.update(title, landingUrl, isActive);
	}

	public void updateDisplayOrders(List<Event> events, List<String> eventIdsInOrder) {
		Map<String, Event> eventById = events.stream()
			.collect(Collectors.toMap(Event::getId, event -> event));

		for (int i = 0; i < eventIdsInOrder.size(); i++) {
			Event event = eventById.get(eventIdsInOrder.get(i));
			if (event != null) {
				event.updateDisplayOrder((i + 1) * Event.DISPLAY_ORDER_INTERVAL);
			}
		}
	}

	public void updateImage(Event event, UuidFile image) {
		event.updateImage(image);
	}

	public void softDelete(Event event) {
		event.softDelete();
	}
}
