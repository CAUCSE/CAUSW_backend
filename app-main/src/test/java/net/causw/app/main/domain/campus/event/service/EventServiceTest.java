package net.causw.app.main.domain.campus.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.implementation.FileWriter;
import net.causw.app.main.domain.campus.event.entity.Event;
import net.causw.app.main.domain.campus.event.service.dto.EventCreateCommand;
import net.causw.app.main.domain.campus.event.service.dto.EventResult;
import net.causw.app.main.domain.campus.event.service.dto.EventUpdateCommand;
import net.causw.app.main.domain.campus.event.service.implementation.EventReader;
import net.causw.app.main.domain.campus.event.service.implementation.EventWriter;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.EventErrorCode;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

	@InjectMocks
	private EventService eventService;

	@Mock
	private EventReader eventReader;

	@Mock
	private EventWriter eventWriter;

	@Mock
	private FileWriter fileWriter;

	private UuidFile mockImage(String uuid, String fileUrl) {
		UuidFile image = mock(UuidFile.class);
		given(image.getUuid()).willReturn(uuid);
		given(image.getFileUrl()).willReturn(fileUrl);
		return image;
	}

	@Nested
	@DisplayName("이벤트 조회 테스트")
	class GetEventsTest {

		@Test
		@DisplayName("활성화된 이벤트만 결과로 변환해 반환한다")
		void givenActiveEvents_whenGetActiveEvents_thenReturnResults() {
			// given
			UuidFile image = mock(UuidFile.class);
			given(image.getFileUrl()).willReturn("https://cdn/event.png");
			Event event = Event.of("ICT 위원회", "https://causw.net", true, 0, image);
			given(eventReader.findAllActive()).willReturn(List.of(event));

			// when
			List<EventResult> results = eventService.getActiveEvents();

			// then
			assertThat(results).hasSize(1);
			assertThat(results.getFirst().title()).isEqualTo("ICT 위원회");
			assertThat(results.getFirst().imageUrl()).isEqualTo("https://cdn/event.png");
			assertThat(results.getFirst().landingUrl()).isEqualTo("https://causw.net");
			assertThat(results.getFirst().isActive()).isTrue();
		}

		@Test
		@DisplayName("활성화된 이벤트가 없으면 빈 목록을 반환한다")
		void givenNoActiveEvents_whenGetActiveEvents_thenReturnEmptyList() {
			// given
			given(eventReader.findAllActive()).willReturn(List.of());

			// when
			List<EventResult> results = eventService.getActiveEvents();

			// then
			assertThat(results).isEmpty();
		}
	}

	@Nested
	@DisplayName("이벤트 등록 테스트")
	class CreateEventTest {

		@Test
		@DisplayName("이미지를 EVENT 경로로 확정한 뒤 이벤트를 저장한다")
		void givenValidCommand_whenCreateEvent_thenConfirmImageAndSave() {
			// given
			UuidFile image = mock(UuidFile.class);
			given(image.getFileUrl()).willReturn("https://cdn/event.png");
			given(fileWriter.confirmFiles(List.of("image-uuid"), FilePath.EVENT)).willReturn(List.of(image));
			given(eventWriter.create(any(Event.class))).willAnswer(invocation -> invocation.getArgument(0));
			EventCreateCommand command = new EventCreateCommand(
				"CCSSAA", "image-uuid", "https://causw.net", true, 1);

			// when
			EventResult result = eventService.createEvent(command);

			// then
			verify(fileWriter).confirmFiles(List.of("image-uuid"), FilePath.EVENT);
			assertThat(result.title()).isEqualTo("CCSSAA");
			assertThat(result.imageUrl()).isEqualTo("https://cdn/event.png");
			assertThat(result.displayOrder()).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("이벤트 수정 테스트")
	class UpdateEventTest {

		@Test
		@DisplayName("이미지 UUID 가 같으면 이미지를 다시 확정하거나 삭제하지 않는다")
		void givenSameImageUuid_whenUpdateEvent_thenKeepImage() {
			// given
			UuidFile currentImage = mockImage("current-uuid", "https://cdn/current.png");
			Event event = Event.of("이전 제목", null, true, 0, currentImage);
			given(eventReader.getById("event-id")).willReturn(event);
			EventUpdateCommand command = new EventUpdateCommand(
				"새 제목", "current-uuid", "https://causw.net", false, 2);

			// when
			eventService.updateEvent("event-id", command);

			// then
			verify(eventWriter).update(event, "새 제목", "https://causw.net", false, 2);
			verify(fileWriter, never()).confirmFiles(anyList(), any(FilePath.class));
			verify(fileWriter, never()).delete(any(UuidFile.class));
		}

		@Test
		@DisplayName("이미지 UUID 가 바뀌면 새 이미지를 확정하고 기존 이미지를 삭제한다")
		void givenNewImageUuid_whenUpdateEvent_thenReplaceImage() {
			// given
			UuidFile currentImage = mockImage("current-uuid", "https://cdn/current.png");
			UuidFile newImage = mock(UuidFile.class);
			Event event = Event.of("제목", null, true, 0, currentImage);
			given(eventReader.getById("event-id")).willReturn(event);
			given(fileWriter.confirmFiles(List.of("new-uuid"), FilePath.EVENT)).willReturn(List.of(newImage));
			EventUpdateCommand command = new EventUpdateCommand("제목", "new-uuid", null, true, 0);

			// when
			eventService.updateEvent("event-id", command);

			// then
			verify(eventWriter).updateImage(event, newImage);
			verify(fileWriter).delete(currentImage);
		}

		@Test
		@DisplayName("존재하지 않는 이벤트를 수정하면 예외가 발생한다")
		void givenUnknownEventId_whenUpdateEvent_thenThrow() {
			// given
			given(eventReader.getById("unknown")).willThrow(EventErrorCode.EVENT_NOT_FOUND.toBaseException());
			EventUpdateCommand command = new EventUpdateCommand("제목", "uuid", null, true, 0);

			// when & then
			assertThatThrownBy(() -> eventService.updateEvent("unknown", command))
				.isInstanceOf(BaseRunTimeV2Exception.class);
			verify(eventWriter, never()).update(any(), any(), any(), any(), any());
		}
	}

	@Nested
	@DisplayName("이벤트 삭제 테스트")
	class DeleteEventTest {

		@Test
		@DisplayName("이벤트를 소프트 삭제한다")
		void givenEvent_whenDeleteEvent_thenSoftDelete() {
			// given
			Event event = Event.of("제목", null, true, 0, mock(UuidFile.class));
			given(eventReader.getById("event-id")).willReturn(event);

			// when
			eventService.deleteEvent("event-id");

			// then
			verify(eventWriter).softDelete(event);
		}
	}
}
