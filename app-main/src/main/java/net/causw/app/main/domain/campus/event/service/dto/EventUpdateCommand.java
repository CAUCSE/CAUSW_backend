package net.causw.app.main.domain.campus.event.service.dto;

public record EventUpdateCommand(
	String title,
	String imageUuid,
	String landingUrl,
	Boolean isActive,
	Integer displayOrder) {
}
