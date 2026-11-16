package net.causw.app.main.domain.campus.event.service.dto;

public record EventCreateCommand(
	String title,
	String landingUrl,
	Boolean isActive) {
}
