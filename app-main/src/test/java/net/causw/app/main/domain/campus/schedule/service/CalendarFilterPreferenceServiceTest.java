package net.causw.app.main.domain.campus.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.campus.schedule.entity.CalendarFilterPreference;
import net.causw.app.main.domain.campus.schedule.enums.ScheduleCategory;
import net.causw.app.main.domain.campus.schedule.service.implementation.CalendarFilterPreferenceReader;
import net.causw.app.main.domain.campus.schedule.service.implementation.CalendarFilterPreferenceWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

@ExtendWith(MockitoExtension.class)
class CalendarFilterPreferenceServiceTest {

	@Mock
	private CalendarFilterPreferenceReader calendarFilterPreferenceReader;

	@Mock
	private CalendarFilterPreferenceWriter calendarFilterPreferenceWriter;

	@Mock
	private UserReader userReader;

	@InjectMocks
	private CalendarFilterPreferenceService calendarFilterPreferenceService;

	private User user;

	@BeforeEach
	void setUp() {
		user = mock(User.class);
	}

	@Test
	@DisplayName("저장된 캘린더 필터가 없으면 빈 카테고리 집합을 반환한다")
	void getCategoriesReturnsEmptySetWhenPreferenceDoesNotExist() {
		String userId = "user-id";

		when(calendarFilterPreferenceReader.findByUserId(userId))
			.thenReturn(Optional.empty());

		Set<ScheduleCategory> result = calendarFilterPreferenceService.getCategories(userId);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("기존 설정이 없으면 새 캘린더 필터 설정을 생성한다")
	void updateCategoriesCreatesNewPreference() {
		String userId = "user-id";
		Set<ScheduleCategory> categories = Set.of(ScheduleCategory.ACADEMIC, ScheduleCategory.CEREMONY);

		when(calendarFilterPreferenceReader.findByUserId(userId))
			.thenReturn(Optional.empty());
		when(userReader.findUserById(userId))
			.thenReturn(user);
		when(calendarFilterPreferenceWriter.save(any(CalendarFilterPreference.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		Set<ScheduleCategory> result = calendarFilterPreferenceService.updateCategories(userId, categories);

		assertThat(result)
			.containsExactlyInAnyOrder(
				ScheduleCategory.ACADEMIC,
				ScheduleCategory.CEREMONY);

		verify(userReader).findUserById(userId);
		verify(calendarFilterPreferenceWriter)
			.save(any(CalendarFilterPreference.class));
	}

	@Test
	@DisplayName("기존 캘린더 필터 설정이 있으면 선택 카테고리를 변경한다")
	void updateCategoriesUpdatesExistingPreference() {
		String userId = "user-id";

		CalendarFilterPreference preference = CalendarFilterPreference.of(user);
		preference.updateCategories(Set.of(ScheduleCategory.ACADEMIC));

		Set<ScheduleCategory> newCategories = Set.of(ScheduleCategory.HOLIDAY, ScheduleCategory.CEREMONY);

		when(calendarFilterPreferenceReader.findByUserId(userId))
			.thenReturn(Optional.of(preference));

		when(calendarFilterPreferenceWriter.save(preference))
			.thenReturn(preference);

		Set<ScheduleCategory> result = calendarFilterPreferenceService.updateCategories(userId, newCategories);

		assertThat(result)
			.containsExactlyInAnyOrder(
				ScheduleCategory.HOLIDAY,
				ScheduleCategory.CEREMONY);

		assertThat(preference.getCategories())
			.containsExactlyInAnyOrder(
				ScheduleCategory.HOLIDAY,
				ScheduleCategory.CEREMONY);
	}

	@Test
	@DisplayName("빈 카테고리 집합으로 저장하면 모든 필터 선택이 해제된다")
	void updateCategoriesClearsAllCategories() {
		String userId = "user-id";

		CalendarFilterPreference preference = CalendarFilterPreference.of(user);
		preference.updateCategories(
			Set.of(
				ScheduleCategory.ACADEMIC,
				ScheduleCategory.CEREMONY));

		when(calendarFilterPreferenceReader.findByUserId(userId))
			.thenReturn(Optional.of(preference));

		when(calendarFilterPreferenceWriter.save(preference))
			.thenReturn(preference);

		Set<ScheduleCategory> result = calendarFilterPreferenceService.updateCategories(
			userId,
			Set.of());

		assertThat(result).isEmpty();
		assertThat(preference.getCategories()).isEmpty();
	}
}