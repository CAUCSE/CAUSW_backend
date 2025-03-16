package net.causw.calendar;

import java.util.Optional;
import net.causw.adapter.persistence.calendar.Calendar;
import net.causw.adapter.persistence.repository.calendar.CalendarRepository;
import net.causw.adapter.persistence.repository.uuidFile.CalendarAttachImageRepository;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.adapter.persistence.uuidFile.joinEntity.CalendarAttachImage;
import net.causw.application.calendar.CalendarService;
import net.causw.application.dto.calendar.CalendarResponseDto;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.model.enums.uuidFile.FilePath;
import net.causw.domain.model.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CalendarServiceTest {

  @InjectMocks
  CalendarService calendarService;

  @Mock
  private CalendarRepository calendarRepository;
  @Mock
  private UuidFileService uuidFileService;
  @Mock
  private CalendarAttachImageRepository calendarAttachImageRepository;


  @Nested
  @DisplayName("Calendar 삭제 테스트")
  class CalendarDeleteTest {

    UuidFile mockUuidFile;
    Calendar mockCalendar;
    CalendarAttachImage mockAttachImage;

    // 각 테스트마다 초기화
    @BeforeEach
    void setUp() {
      mockUuidFile = UuidFile.of(
          "uuid",
          "fileKey",
          "fileUrl",
          "rawFileName",
          "png",
          FilePath.CALENDAR
      );
      mockCalendar = Calendar.of(2025, 3, mockUuidFile);
      mockAttachImage = mockCalendar.getCalendarAttachImage();


    }

    @Test
    @DisplayName("Calendar 삭제 성공")
    void deleteCalendarSuccess() {
      // given
      String calendarId = "testId";
      given(calendarRepository.findById(calendarId)).willReturn(Optional.of(mockCalendar));

      // when
      CalendarResponseDto result = calendarService.deleteCalendar(calendarId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getYear()).isEqualTo(2025);
      assertThat(result.getMonth()).isEqualTo(3);

      verify(calendarRepository, times(1)).findById(calendarId);
      verify(calendarAttachImageRepository, times(1)).delete(mockAttachImage);
      verify(uuidFileService, times(1)).deleteFile(mockUuidFile);
      verify(calendarRepository, times(1)).delete(mockCalendar);
    }

    @Test
    @DisplayName("Calendar 없을 시 삭제 실패")
    void deleteCalendarFailedWhenCalendarNotExist() {
      // given
      String calendarId = "notExistsId";
      given(calendarRepository.findById(calendarId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> calendarService.deleteCalendar(calendarId))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining(MessageUtil.CALENDAR_NOT_FOUND);

      verify(calendarRepository, times(1)).findById(calendarId);
      verify(calendarAttachImageRepository, never()).delete(any());
      verify(uuidFileService, never()).deleteFile(any());
      verify(calendarRepository, never()).delete(any());
    }
  }

}
