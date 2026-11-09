package net.causw.app.main.domain.campus.event.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.campus.event.entity.Event;
import net.causw.app.main.domain.campus.event.event.EventImageDeletionRequestedEvent;
import net.causw.app.main.domain.campus.event.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventWriterTest {

	@InjectMocks
	private EventWriter eventWriter;

	@Mock
	private EventRepository eventRepository;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	private Event eventWithId(String id, int displayOrder) {
		Event event = Event.of("제목", null, true, displayOrder, mock(UuidFile.class));
		ReflectionTestUtils.setField(event, "id", id);
		return event;
	}

	@Test
	@DisplayName("요청한 ID 순서대로 10 간격의 노출 순서를 다시 지정한다")
	void givenEventIds_whenUpdateDisplayOrders_thenReassignInOrder() {
		Event first = eventWithId("first", 10);
		Event second = eventWithId("second", 20);

		eventWriter.updateDisplayOrders(List.of(first, second), List.of("second", "first"));

		assertThat(second.getDisplayOrder()).isEqualTo(10);
		assertThat(first.getDisplayOrder()).isEqualTo(20);
	}

	@Test
	@DisplayName("조회되지 않은 ID 는 순서 자리만 차지하고 건너뛴다")
	void givenUnknownEventId_whenUpdateDisplayOrders_thenSkip() {
		Event event = eventWithId("event", 10);

		eventWriter.updateDisplayOrders(List.of(event), List.of("deleted", "event"));

		assertThat(event.getDisplayOrder()).isEqualTo(20);
	}

	@Test
	@DisplayName("이미지 파일 삭제 요청 이벤트를 파일 키와 함께 발행한다")
	void givenImage_whenRequestImageFileDeletion_thenPublishEvent() {
		UuidFile image = mock(UuidFile.class);
		given(image.getFileKey()).willReturn("event/old.png");

		eventWriter.requestImageFileDeletion(image);

		verify(applicationEventPublisher).publishEvent(new EventImageDeletionRequestedEvent("event/old.png"));
	}
}
