package net.causw.app.main.domain.campus.schedule.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.entity.CalendarFilterPreference;
import net.causw.app.main.domain.campus.schedule.enums.ScheduleCategory;
import net.causw.app.main.domain.campus.schedule.service.implementation.CalendarFilterPreferenceReader;
import net.causw.app.main.domain.campus.schedule.service.implementation.CalendarFilterPreferenceWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarFilterPreferenceService {

	private final CalendarFilterPreferenceReader calendarFilterPreferenceReader;
	private final CalendarFilterPreferenceWriter calendarFilterPreferenceWriter;
	private final UserReader userReader;

	@Transactional(readOnly = true)
	public Set<ScheduleCategory> getCategories(String userId) {
		return calendarFilterPreferenceReader.findByUserId(userId)
			.map(preference -> new HashSet<>(preference.getCategories()))
			.orElseGet(HashSet::new);
	}

	@Transactional
	public Set<ScheduleCategory> updateCategories(
		String userId,
		Set<ScheduleCategory> categories) {

		CalendarFilterPreference preference = calendarFilterPreferenceReader.findByUserId(userId)
			.orElseGet(() -> {
				User user = userReader.findUserById(userId);
				return CalendarFilterPreference.of(user);
			});

		preference.updateCategories(categories);

		CalendarFilterPreference savedPreference = calendarFilterPreferenceWriter.save(preference);

		return new HashSet<>(savedPreference.getCategories());
	}
}