package net.causw.app.main.service.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.model.entity.event.Event;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.enums.uuidFile.FilePath;
import net.causw.app.main.dto.event.EventCreateRequestDto;
import net.causw.app.main.dto.event.EventResponseDto;
import net.causw.app.main.dto.event.EventUpdateRequestDto;
import net.causw.app.main.dto.event.EventsResponseDto;
import net.causw.app.main.repository.event.EventRepository;
import net.causw.app.main.service.uuidFile.UuidFileService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

	@InjectMocks
	private EventService eventService;

	@Mock
	private EventRepository eventRepository;

	@Mock
	private UuidFileService uuidFileService;

	@Nested
	@DisplayName("이벤트 리스트 테스트")
	class EventListTest {

		List<Event> mockEvents;

		@BeforeEach
		void setUp() {
			UuidFile mockUuidFile = UuidFile.of(
				"uuid",
				"fileKey",
				"fileUrl",
				"rawFileName",
				"png",
				FilePath.EVENT
			);
			mockEvents = List.of(
				Event.of("url2", mockUuidFile, false),
				Event.of("url1", mockUuidFile, false)
			);
		}

		@Test
		@DisplayName("이벤트 리스트 성공")
		void listEventSuccess() {

			// given
			given(eventRepository.findByIsDeletedIsFalseOrderByCreatedAtDesc()).willReturn(mockEvents);

			// when
			EventsResponseDto result = eventService.findEvents();

			// then
			assertThat(result).isNotNull();
			assertThat(result.getEvents().size()).isEqualTo(2);
			assertThat(result.getEvents().get(0).getUrl()).isEqualTo("url2");
		}

		@Test
		@DisplayName("이벤트가 없을 때 빈 리스트 반환")
		void listEventEmpty() {
			// given
			given(eventRepository.findByIsDeletedIsFalseOrderByCreatedAtDesc()).willReturn(List.of());

			// when
			EventsResponseDto result = eventService.findEvents();

			// then
			assertThat(result).isNotNull();
			assertThat(result.getEvents()).isEmpty();

			verify(eventRepository).findByIsDeletedIsFalseOrderByCreatedAtDesc();
		}
	}

	@Nested
	@DisplayName("이벤트 생성 테스트")
	class EventCreateTest {

		EventCreateRequestDto mockEventCreateRequestDto;
		MultipartFile mockMultiFile;
		UuidFile mockUuidFile;
		Event mockEvent;

		@BeforeEach
		void setUp() {
			mockEventCreateRequestDto = new EventCreateRequestDto("url1");
			mockMultiFile = new MockMultipartFile(
				"image1"
				, "image" + ".png",
				"png",
				"file".getBytes()
			);
			mockUuidFile = UuidFile.of(
				"uuid",
				"fileKey",
				"fileUrl",
				mockMultiFile.getName(),
				mockMultiFile.getContentType(),
				FilePath.CALENDAR
			);

			mockEvent = Event.of(
				mockEventCreateRequestDto.getUrl(),
				mockUuidFile,
				false
			);
		}

		@Test
		@DisplayName("이벤트 생성 성공")
		void createEventSuccess() {

			// given
			given(eventRepository.findByIsDeletedIsFalseOrderByCreatedAtDesc())
				.willReturn(List.of());
			given(eventRepository.save(any(Event.class))).willReturn(mockEvent);
			given(uuidFileService.saveFile(mockMultiFile, FilePath.EVENT))
				.willReturn(mockUuidFile);

			// when
			EventResponseDto result = eventService.createEvent(mockEventCreateRequestDto, mockMultiFile);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getImage()).isEqualTo(mockUuidFile.getFileUrl());
			assertThat(result.getUrl()).isEqualTo(mockEvent.getUrl());

			verify(uuidFileService).saveFile(mockMultiFile, FilePath.EVENT);
			verify(eventRepository).save(any(Event.class));
		}

		@Test
		@DisplayName("최대 이벤트 개수 초과로 인한 이벤트 생성 실패")
		void createEventFailed() {

			int maxEventSize = StaticValue.MAX_NUM_EVENT;
			// given
			List<Event> mockEvents = IntStream.range(0, maxEventSize)
				.mapToObj(i -> Event.of(
					mockEventCreateRequestDto.getUrl() + i, // 각 이벤트마다 URL 다르게 설정
					mockUuidFile,
					false
				))
				.toList();
			given(eventRepository.findByIsDeletedIsFalseOrderByCreatedAtDesc())
				.willReturn(mockEvents);

			// when & then
			assertThatThrownBy(() -> eventService.createEvent(mockEventCreateRequestDto, mockMultiFile))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.EVENT_MAX_CREATED)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.CANNOT_PERFORMED);

			verify(uuidFileService, never()).saveFile(any(), any());
			verify(eventRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("이벤트 수정 테스트")
	class EventUpdateTest {

		private static final String MOCK_URL = "url1";
		private static final String MOCK_EVENT_ID = "id1";

		EventUpdateRequestDto eventUpdateRequestDto;
		MultipartFile previousMockMultipartFile;
		MultipartFile toBeUpdateMockMultipartFile;
		UuidFile mockUuidFile;
		Event mockEvent;

		@BeforeEach
		void setUp() {
			eventUpdateRequestDto = new EventUpdateRequestDto(MOCK_URL);

			previousMockMultipartFile = createMockMultipartFile("image1", "file1");
			toBeUpdateMockMultipartFile = createMockMultipartFile("image2", "file2");

			mockUuidFile = UuidFile.of(
				"uuid", "fileKey", "fileUrl",
				previousMockMultipartFile.getName(),
				previousMockMultipartFile.getContentType(),
				FilePath.CALENDAR
			);

			mockEvent = Event.of(eventUpdateRequestDto.getUrl(), mockUuidFile, false);
		}

		@Test
		@DisplayName("이벤트 수정 성공")
		void updateEventSuccess() {

			// given
			givenWhenPreviousEventExist();
			UuidFile updatedUuidFile = UuidFile.of(
				"updatedUuid",
				"updatedFileKey",
				"updatedFileUrl",
				toBeUpdateMockMultipartFile.getName(),
				toBeUpdateMockMultipartFile.getContentType(),
				FilePath.EVENT
			);

			given(uuidFileService.updateFile(mockUuidFile, toBeUpdateMockMultipartFile,
				FilePath.EVENT)).willReturn(updatedUuidFile);

			// when
			EventResponseDto result = eventService.updateEvent(MOCK_EVENT_ID,
				eventUpdateRequestDto,
				toBeUpdateMockMultipartFile);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getImage()).isEqualTo(updatedUuidFile.getFileUrl());
			assertThat(result.getUrl()).isEqualTo(eventUpdateRequestDto.getUrl());

			verify(eventRepository).findById(MOCK_EVENT_ID);
			verify(uuidFileService).updateFile(mockUuidFile, toBeUpdateMockMultipartFile, FilePath.EVENT);
			verify(eventRepository).save(any(Event.class));
		}

		@Test
		@DisplayName("이미지 수정 없이 이벤트 수정 성공")
		void updateEventSuccess_WhenFileIsNull() {

			// given
			givenWhenPreviousEventExist();

			// when
			EventResponseDto result = eventService.updateEvent(MOCK_EVENT_ID, eventUpdateRequestDto,
				null);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getImage()).isEqualTo(mockUuidFile.getFileUrl());
			assertThat(result.getUrl()).isEqualTo(eventUpdateRequestDto.getUrl());

			verify(eventRepository).findById(MOCK_EVENT_ID);
			verify(uuidFileService, never()).updateFile(any(), any(), any());
			verify(eventRepository).save(any(Event.class));
		}

		@Test
		@DisplayName("기존 이벤트 없을 시 이벤트 수정 실패")
		void updateEventFailed_WhenEventNotExist() {

			// given
			String notExistId = "notExistId";
			given(eventRepository.findById(notExistId)).willReturn(Optional.empty());

			// when
			assertThatThrownBy(() -> eventService.updateEvent(notExistId, eventUpdateRequestDto,
				toBeUpdateMockMultipartFile))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.EVENT_NOT_FOUND)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);
		}

		private void givenWhenPreviousEventExist() {
			given(eventRepository.findById(MOCK_EVENT_ID)).willReturn(Optional.of(mockEvent));
			given(eventRepository.save(any(Event.class)))
				.willAnswer(invocation -> invocation.getArgument(0));
		}

		private MultipartFile createMockMultipartFile(final String name, final String content) {
			return new MockMultipartFile(
				name, name + ".png", "png", content.getBytes()
			);
		}
	}

	@Nested
	@DisplayName("이벤트 삭제 테스트")
	class EventDeleteTest {

		private static final String MOCK_URL = "url1";
		private static final String MOCK_EVENT_ID = "id1";

		EventUpdateRequestDto eventUpdateRequestDto;
		MultipartFile previousMockMultipartFile;
		UuidFile mockUuidFile;
		Event mockEvent;

		@BeforeEach
		void setUp() {
			eventUpdateRequestDto = new EventUpdateRequestDto(MOCK_URL);
			previousMockMultipartFile = createMockMultipartFile("image1", "file1");

			mockUuidFile = UuidFile.of(
				"uuid", "fileKey", "fileUrl",
				previousMockMultipartFile.getName(),
				previousMockMultipartFile.getContentType(),
				FilePath.CALENDAR
			);

			mockEvent = Event.of(eventUpdateRequestDto.getUrl(), mockUuidFile, false);
		}

		@Test
		@DisplayName("이벤트 삭제 성공")
		void deleteEventSuccess() {

			// given
			givenWhenPreviousEventExist();

			// when
			EventResponseDto result = eventService.deleteEvent(MOCK_EVENT_ID);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getImage()).isEqualTo(mockUuidFile.getFileUrl());
			assertThat(result.getUrl()).isEqualTo(eventUpdateRequestDto.getUrl());
			assertThat(result.getIsDeleted()).isEqualTo(true);

			verify(eventRepository).findById(MOCK_EVENT_ID);
			verify(eventRepository).save(any(Event.class));
		}

		@Test
		@DisplayName("기존 이벤트 없을 시 이벤트 삭제 실패")
		void updateEventFailed_WhenEventNotExist() {

			// given
			String notExistId = "notExistId";
			given(eventRepository.findById(notExistId)).willReturn(Optional.empty());

			// when
			assertThatThrownBy(() -> eventService.deleteEvent(notExistId))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.EVENT_NOT_FOUND)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);
		}

		private void givenWhenPreviousEventExist() {
			given(eventRepository.findById(MOCK_EVENT_ID)).willReturn(Optional.of(mockEvent));
			given(eventRepository.save(any(Event.class)))
				.willAnswer(invocation -> invocation.getArgument(0));
		}

		private MultipartFile createMockMultipartFile(final String name, final String content) {
			return new MockMultipartFile(
				name, name + ".png", "png", content.getBytes()
			);
		}
	}
}
