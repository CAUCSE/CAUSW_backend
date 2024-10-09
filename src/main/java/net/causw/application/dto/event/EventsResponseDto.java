package net.causw.application.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class EventsResponseDto {

    @Schema(description = "개수", example = "7")
    private Integer count;

    @Schema(description = "이벤트 목록")
    private List<EventResponseDto> events;
}
