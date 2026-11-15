package net.causw.app.main.domain.campus.schedule.api.v2.dto.response;

import java.util.Set;

import net.causw.app.main.domain.campus.schedule.enums.ScheduleCategory;

public record CalendarFilterPreferenceResponse(
	Set<ScheduleCategory> categories) {

	public static CalendarFilterPreferenceResponse from(Set<ScheduleCategory> categories) {
		return new CalendarFilterPreferenceResponse(categories);
	}
}