package net.causw.app.main.domain.campus.schedule.api.v2.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.campus.schedule.enums.ScheduleCategory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CalendarFilterPreferenceRequestTest {

	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	@DisplayName("categories에 null 원소가 포함되면 validation에 실패한다")
	void validateNullCategoryElement() {
		// given
		Set<ScheduleCategory> categories = new HashSet<>();
		categories.add(ScheduleCategory.ACADEMIC);
		categories.add(null);

		CalendarFilterPreferenceRequest request = new CalendarFilterPreferenceRequest(categories);

		// when
		Set<ConstraintViolation<CalendarFilterPreferenceRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
	}
}