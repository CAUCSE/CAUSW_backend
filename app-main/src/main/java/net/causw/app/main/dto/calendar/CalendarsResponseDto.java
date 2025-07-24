package net.causw.app.main.dto.calendar;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CalendarsResponseDto {

	@Schema(description = "개수", example = "7")
	private Integer count;

	@Schema(description = "캘린더 목록")
	private List<CalendarResponseDto> calendars;
}
