package net.causw.app.main.domain.campus.schedule.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.entity.CalendarFilterPreference;
import net.causw.app.main.domain.campus.schedule.repository.CalendarFilterPreferenceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarFilterPreferenceReader {

	private final CalendarFilterPreferenceRepository calendarFilterPreferenceRepository;

	public Optional<CalendarFilterPreference> findByUserId(String userId) {
		return calendarFilterPreferenceRepository.findByUserId(userId);
	}
}