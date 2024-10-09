package net.causw.application.dto.calendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CalendarResponseDto {

    @Schema(description = "캘린더 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "년도", example = "2024")
    private Integer year;

    @Schema(description = "월", example = "9")
    private Integer month;

    @Schema(description = "이미지", example = "")
    private String image;

    @Schema(description = "캘린더 생성 시간", example = "2024-01-26T18:40:40.643Z")
    private String createdAt;

    @Schema(description = "캘린더 업데이트 시간", example = "2024-01-26T18:40:40.643Z")
    private String updatedAt;
}
