package net.causw.app.main.domain.asset.locker.service.v2.dto.result;

import java.util.List;

import net.causw.app.main.domain.asset.locker.entity.LockerStatus;

import lombok.Builder;

/**
 * 사물함 위치(층)별 상세 정보 조회 결과 DTO.
 *
 * @param floor         층(위치) 정보
 * @param currentPolicy 현재 적용 중인 사물함 정책 요약
 * @param summary       해당 위치의 사물함 요약 정보
 * @param lockers       개별 사물함 목록
 */
@Builder
public record LockerLocationResult(
	FloorResult floor,
	PolicyResult currentPolicy,
	SummaryResult summary,
	List<LockerItemResult> lockers) {

	/**
	 * 층(위치) 기본 정보를 담는 DTO.
	 *
	 * @param locationId          위치 ID
	 * @param locationName        위치 이름 (예: 2층, 지하 등)
	 * @param locationDescription 위치 상세 설명
	 */
	@Builder
	public record FloorResult(String locationId, String locationName, String locationDescription) {
	}

	/**
	 * 사물함 정책 요약 정보를 담는 DTO.
	 *
	 * @param canApply  현재 사물함 신청 가능 여부
	 * @param canExtend 현재 사물함 연장 가능 여부
	 */
	@Builder
	public record PolicyResult(boolean canApply, boolean canExtend) {
	}

	/**
	 * 위치별 사물함 요약 정보를 담는 DTO.
	 *
	 * @param totalCount     전체 사물함 수
	 * @param availableCount 신청 가능(비어 있는) 사물함 수
	 */
	@Builder
	public record SummaryResult(long totalCount, long availableCount) {
	}

	/**
	 * 개별 사물함 정보를 담는 DTO.
	 *
	 * @param lockerId 사물함 ID
	 * @param number   사물함 번호
	 * @param status   사물함 상태
	 */
	@Builder
	public record LockerItemResult(String lockerId, String number, LockerStatus status) {
	}
}
