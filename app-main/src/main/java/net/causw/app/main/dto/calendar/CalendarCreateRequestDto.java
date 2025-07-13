package net.causw.app.main.dto.calendar;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CalendarCreateRequestDto {

    @NotNull(message = "캘린더 년도는 필수 입력값 입니다.")
    @Schema(description = "년도", example = "2024")
    private Integer year;

    @NotNull(message = "캘린더 월은 필수 입력값 입니다.")
    @Schema(description = "월", example = "9")
    private Integer month;

}
