package net.causw.application.dto.calendar;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CalendarDeleteRequestDto {

    @Schema(description = "삭제할 캘린더 Id 배열", example = "[1, 2]")
    private List<Integer> calendarIds;

}
