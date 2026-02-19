package net.causw.app.main.domain.campus.schedule.api.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "일정 목록 응답")
public record ScheduleListResponse(
	@Schema(description = "조회된 일정 개수", example = "3") Integer count,

	@Schema(description = "일정 목록") List<ScheduleResponse> data) {
}
