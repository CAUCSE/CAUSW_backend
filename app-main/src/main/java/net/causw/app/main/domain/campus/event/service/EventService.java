package net.causw.app.main.domain.campus.event.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.implementation.FileWriter;
import net.causw.app.main.domain.campus.event.entity.Event;
import net.causw.app.main.domain.campus.event.service.dto.EventCreateCommand;
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
	private final FileWriter fileWriter;

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
		UuidFile image = confirmImage(command.imageUuid());
		Event event = eventWriter.create(Event.of(
			command.title(), command.landingUrl(), command.isActive(), command.displayOrder(), image));
		return EventResult.from(event);
	}

	@Transactional
	public EventResult updateEvent(String eventId, EventUpdateCommand command) {
		Event event = eventReader.getById(eventId);
		eventWriter.update(event, command.title(), command.landingUrl(), command.isActive(), command.displayOrder());

		UuidFile currentImage = event.getImage();
		if (!currentImage.getUuid().equals(command.imageUuid())) {
			eventWriter.updateImage(event, confirmImage(command.imageUuid()));
			fileWriter.delete(currentImage);
		}
		return EventResult.from(event);
	}

	@Transactional
	public void deleteEvent(String eventId) {
		Event event = eventReader.getById(eventId);
		eventWriter.softDelete(event);
	}

	private UuidFile confirmImage(String imageUuid) {
		return fileWriter.confirmFiles(List.of(imageUuid), FilePath.EVENT).getFirst();
	}
}
