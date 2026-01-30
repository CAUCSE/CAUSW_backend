package net.causw.app.main.domain.campus.schedule.api.v2.controller.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record ScheduleListResponse(
	Integer count,
	List<ScheduleResponse> data) {
}
