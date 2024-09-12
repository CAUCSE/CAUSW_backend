package net.causw.application.dto.calendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CalendarsResponseDto {

    @Schema(description = "개수", example = "7")
    private Integer count;

    @Schema(description = "캘린더 목록")
    private List<CalendarResponseDto> calendars;
}
