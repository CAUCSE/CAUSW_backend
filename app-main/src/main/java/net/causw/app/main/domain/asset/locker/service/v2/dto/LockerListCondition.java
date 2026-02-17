package net.causw.app.main.domain.asset.locker.service.v2.dto;

import net.causw.app.main.domain.asset.locker.entity.LockerName;

/**
 * 사물함 목록 조회를 위한 검색 조건 DTO.
 *
 * @param userKeyword 유저 검색 키워드 (이름/이메일/학번 등)
 * @param location    사물함 위치(층)
 * @param isActive    활성화 여부 필터
 * @param isOccupied  사용중 여부 필터
 * @param isExpired   만료 여부 필터
 */
public record LockerListCondition(
	String userKeyword,
	LockerName location,
	Boolean isActive,
	Boolean isOccupied,
	Boolean isExpired) {
}
