package net.causw.app.main.domain.campus.schedule.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.entity.CalendarFilterPreference;
import net.causw.app.main.domain.campus.schedule.repository.CalendarFilterPreferenceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CalendarFilterPreferenceWriter {

	private final CalendarFilterPreferenceRepository calendarFilterPreferenceRepository;

	@Transactional
	public CalendarFilterPreference save(CalendarFilterPreference preference) {
		return calendarFilterPreferenceRepository.save(preference);
	}
}