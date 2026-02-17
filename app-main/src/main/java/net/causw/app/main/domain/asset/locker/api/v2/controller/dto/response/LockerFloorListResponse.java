package net.causw.app.main.domain.asset.locker.api.v2.controller.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사물함 전체 층 리스트 응답")
public record LockerFloorListResponse(

	@Schema(description = "전체 사물함 집계 정보") SummaryInfo summary,
	@Schema(description = "층별 사물함 정보 목록") List<FloorItem> floors) {

	@Schema(description = "전체 사물함 집계")
	public record SummaryInfo(
		@Schema(description = "전체 사물함 수", example = "336") long totalCount,
		@Schema(description = "신청 가능 사물함 수", example = "332") long availableCount) {
	}

	@Schema(description = "층별 사물함 정보")
	public record FloorItem(
		@Schema(description = "층 ID", example = "location-uuid-1234") String locationId,
		@Schema(description = "층 이름", example = "2층") String floorName,
		@Schema(description = "전체 사물함 수", example = "136") long totalCount,
		@Schema(description = "잔여 사물함 수", example = "68") long availableCount,
		@Schema(description = "신청 가능 여부", example = "true") boolean isAppliable) {
	}
}
