package net.causw.app.main.domain.campus.event.api.v2.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class EventRequestValidationTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeAll
	static void setUp() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterAll
	static void tearDown() {
		factory.close();
	}

	@ParameterizedTest
	@ValueSource(strings = {"http://causw.net", "https://causw.net"})
	@DisplayName("랜딩 링크가 http(s) 형식이면 검증을 통과한다")
	void givenValidLandingUrl_whenValidate_thenNoViolation(String landingUrl) {
		assertThat(validator.validate(createRequest(landingUrl))).isEmpty();
		assertThat(validator.validate(updateRequest(landingUrl))).isEmpty();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "causw.net", "ftp://causw.net", "https://"})
	@DisplayName("랜딩 링크가 없거나 http(s) 형식이 아니면 검증에 실패한다")
	void givenInvalidLandingUrl_whenValidate_thenViolation(String landingUrl) {
		assertThat(validator.validate(createRequest(landingUrl))).isNotEmpty();
		assertThat(validator.validate(updateRequest(landingUrl))).isNotEmpty();
	}

	private EventCreateRequest createRequest(String landingUrl) {
		return new EventCreateRequest("제목", landingUrl, true);
	}

	private EventUpdateRequest updateRequest(String landingUrl) {
		return new EventUpdateRequest("제목", landingUrl, true);
	}
}
