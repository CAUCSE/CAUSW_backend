package net.causw.app.main.domain.campus.schedule.enums;

import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;

public enum ScheduleCategory {
	ACADEMIC,
	DEPARTMENT,
	CCSSAA,
	STUDENT_COUNCIL,
	HOLIDAY,
	CEREMONY;

	public static ScheduleCategory from(ScheduleType scheduleType) {
		return ScheduleCategory.valueOf(scheduleType.name());
	}
}