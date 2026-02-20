package net.causw.app.main.domain.asset.locker.service.v2.dto;

import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.enums.LockerLogAction;

/**
 * 사물함 로그 목록 조회를 위한 검색 조건 DTO.
 *
 * @param userKeyword        유저 검색 키워드 (이름/이메일 등)
 * @param action             사물함 로그 액션 타입
 * @param lockerLocationName 사물함 위치(층) 이름
 * @param lockerNumber       사물함 번호
 */
public record LockerLogListCondition(
	String userKeyword,
	LockerLogAction action,
	LockerName lockerLocationName,
	Long lockerNumber) {
}
