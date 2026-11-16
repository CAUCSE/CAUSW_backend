package net.causw.app.main.domain.campus.event.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.campus.event.entity.Event;

public record EventResult(
	String id,
	String title,
	String landingUrl,
	Boolean isActive,
	Integer displayOrder,
	LocalDateTime createdAt,
	LocalDateTime updatedAt) {

	public static EventResult from(Event event) {
		return new EventResult(
			event.getId(),
			event.getTitle(),
			event.getLandingUrl(),
			event.getIsActive(),
			event.getDisplayOrder(),
			event.getCreatedAt(),
			event.getUpdatedAt());
	}
}
