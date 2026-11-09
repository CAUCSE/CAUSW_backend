package net.causw.app.main.domain.campus.schedule.api.v2.dto.request;

import java.util.Set;

import net.causw.app.main.domain.campus.schedule.enums.ScheduleCategory;

import jakarta.validation.constraints.NotNull;

public record CalendarFilterPreferenceRequest(
	@NotNull Set<ScheduleCategory> categories) {
}