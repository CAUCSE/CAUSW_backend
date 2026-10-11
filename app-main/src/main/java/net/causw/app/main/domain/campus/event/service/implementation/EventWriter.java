package net.causw.app.main.domain.campus.event.service.implementation;

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

	public void update(Event event, String title, String landingUrl, Boolean isActive, Integer displayOrder) {
		event.update(title, landingUrl, isActive, displayOrder);
	}

	public void updateImage(Event event, UuidFile image) {
		event.updateImage(image);
	}

	public void softDelete(Event event) {
		event.softDelete();
	}
}
