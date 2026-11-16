package net.causw.app.main.domain.campus.event.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.campus.event.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventReaderTest {

	@InjectMocks
	private EventReader eventReader;

	@Mock
	private EventRepository eventRepository;

	@Test
	@DisplayName("이벤트가 없으면 다음 노출 순서는 10 이다")
	void givenNoEvent_whenGetNextDisplayOrder_thenInterval() {
		given(eventRepository.findMaxDisplayOrder()).willReturn(null);

		assertThat(eventReader.getNextDisplayOrder()).isEqualTo(10);
	}

	@Test
	@DisplayName("다음 노출 순서는 가장 큰 순서에 10 을 더한 값이다")
	void givenMaxDisplayOrder_whenGetNextDisplayOrder_thenMaxPlusInterval() {
		given(eventRepository.findMaxDisplayOrder()).willReturn(30);

		assertThat(eventReader.getNextDisplayOrder()).isEqualTo(40);
	}
}
