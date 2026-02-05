package net.causw.app.main.domain.campus.schedule.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "일정 응답")
public record ScheduleResponse(
	@Schema(description = "일정 ID", example = "550e8400-e29b-41d4-a716-446655440000") String id,

	@Schema(description = "일정 제목", example = "중간고사 기간") String title,

	@Schema(description = "일정 유형", example = "ACADEMIC") ScheduleType type,

	@Schema(description = "일정 시작 시간", example = "2026-04-15T00:00:00") LocalDateTime start,

	@Schema(description = "일정 종료 시간", example = "2026-04-21T23:59:59") LocalDateTime end) {
}
