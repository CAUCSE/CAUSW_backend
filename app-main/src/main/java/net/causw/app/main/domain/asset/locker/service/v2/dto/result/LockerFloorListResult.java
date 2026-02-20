package net.causw.app.main.domain.asset.locker.service.v2.dto.result;

import java.util.List;

import lombok.Builder;

/**
 * 층(위치)별 사물함 현황 리스트 결과 DTO.
 *
 * @param summary 전체 사물함 요약 정보
 * @param floors  층별 사물함 요약 목록
 */
@Builder
public record LockerFloorListResult(
	SummaryResult summary,
	List<FloorItemResult> floors) {

	/**
	 * 전체 사물함 요약 정보를 담는 DTO.
	 *
	 * @param totalCount     전체 사물함 수
	 * @param availableCount 신청 가능(비어 있는) 사물함 수
	 */
	@Builder
	public record SummaryResult(long totalCount, long availableCount) {
	}

	/**
	 * 층(위치)별 사물함 요약 정보를 담는 DTO.
	 *
	 * @param locationId     위치 ID
	 * @param floorName      층 이름
	 * @param totalCount     해당 층 전체 사물함 수
	 * @param availableCount 해당 층 신청 가능(비어 있는) 사물함 수
	 */
	@Builder
	public record FloorItemResult(
		String locationId,
		String floorName,
		long totalCount,
		long availableCount) {
	}
}
