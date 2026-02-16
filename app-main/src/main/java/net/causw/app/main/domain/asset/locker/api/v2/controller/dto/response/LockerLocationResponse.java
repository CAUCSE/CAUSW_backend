package net.causw.app.main.domain.asset.locker.api.v2.controller.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "층별 사물함 조회 응답")
public record LockerLocationResponse(

	@Schema(description = "층 정보") FloorInfo floor,

	@Schema(description = "현재 정책 상태") PolicyInfo currentPolicy,

	@Schema(description = "사물함 집계 정보") SummaryInfo summary,

	@Schema(description = "사물함 목록") List<LockerItem> lockers,

	@Schema(description = "현재 유저 가능 액션") ActionInfo actions) {

	@Schema(description = "층 정보")
	public record FloorInfo(

		@Schema(description = "위치 ID", example = "location-uuid-1234") String locationId,

		@Schema(description = "위치명", example = "4층") String locationName) {
	}

	@Schema(description = "정책 상태")
	public record PolicyInfo(

		@Schema(description = "신청 가능 기간 여부", example = "true") boolean canApply,

		@Schema(description = "연장 가능 기간 여부", example = "false") boolean canExtend) {
	}

	@Schema(description = "사물함 집계")
	public record SummaryInfo(

		@Schema(description = "전체 사물함 수", example = "30") long totalCount,

		@Schema(description = "사용 가능 사물함 수", example = "12") long availableCount) {
	}

	@Schema(description = "사물함 항목")
	public record LockerItem(

		@Schema(description = "사물함 ID", example = "locker-uuid-1234") String lockerId,

		@Schema(description = "사물함 번호", example = "15") String number,

		@Schema(description = "사물함 상태", example = "AVAILABLE", allowableValues = {
			"AVAILABLE", "MINE", "IN_USE", "DISABLED"}) String status){
	}

	@Schema(description = "유저 액션 가능 여부")
	public record ActionInfo(

		@Schema(description = "신청 가능 여부", example = "true") boolean canApply,

		@Schema(description = "반납 가능 여부", example = "false") boolean canReturn,

		@Schema(description = "연장 가능 여부", example = "false") boolean canExtend) {
	}
}
