package net.causw.app.main.domain.campus.event.service.dto;

import java.util.List;

public record EventOrderUpdateCommand(
	List<String> eventIds) {
}
