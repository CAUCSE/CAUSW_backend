package net.causw.app.main.domain.campus.event.api.v2.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record EventOrderUpdateRequest(
	@Schema(description = "노출 순서대로 나열한 이벤트 ID 목록", example = "[\"event-id-1\", \"event-id-2\"]") @NotNull List<String> eventIds) {
}
