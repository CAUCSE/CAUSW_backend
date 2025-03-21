package net.causw.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import net.causw.adapter.persistence.calendar.Calendar;
import net.causw.adapter.persistence.event.Event;
import net.causw.adapter.persistence.repository.event.EventRepository;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.event.EventResponseDto;
import net.causw.application.dto.event.EventsResponseDto;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.model.enums.uuidFile.FilePath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

  @InjectMocks
  private EventService eventService;

  @Mock
  private EventRepository eventRepository;

  @Mock
  private UuidFileService uuidFileService;

  @Nested
  @DisplayName("배너 리스트 테스트")
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
          FilePath.CALENDAR
      );
      mockEvents = List.of(
          Event.of("url1", mockUuidFile, false),
          Event.of("url2", mockUuidFile, false)
      );
    }

    @Test
    @DisplayName("배너 리스트 성공")
    void listEventBannerSuccess() {

      // given
      given(eventRepository.findByIsDeletedIsFalse()).willReturn(mockEvents);

      // when
      EventsResponseDto result = eventService.findEvents();

      // then
      assertThat(result).isNotNull();
      assertThat(result.getEvents().size()).isEqualTo(2);
      assertThat(result.getEvents().getFirst().getUrl()).isEqualTo("url1");
    }

    @Test
    @DisplayName("이벤트가 없을 때 빈 리스트 반환")
    void listEventBannerEmpty() {
      // given
      given(eventRepository.findByIsDeletedIsFalse()).willReturn(List.of());

      // when
      EventsResponseDto result = eventService.findEvents();

      // then
      assertThat(result).isNotNull();
      assertThat(result.getEvents()).isEmpty();
    }

  }
}
