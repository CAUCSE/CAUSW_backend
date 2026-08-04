package net.causw.app.main.domain.user.account.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.account.enums.userinfo.SortType;
import net.causw.app.main.domain.user.account.enums.userinfo.UserInfoSectionType;

public record UserInfoCursor(
	UserInfoSectionType section,
	SortType sortType,
	LocalDateTime updatedAt,
	Integer admissionYear,
	String name,
	String userInfoId,
	String filterHash) {

	/**
	 * 지정한 섹션의 첫 항목부터 조회하기 위한 커서를 생성한다.
	 * @param section 조회를 시작할 섹션
	 * @param sortType 정렬 기준
	 * @param filterHash 조회 조건 식별 hash
	 * @return 섹션 시작 커서
	 */
	public static UserInfoCursor sectionStartCursor(
		UserInfoSectionType section,
		SortType sortType,
		String filterHash) {
		return new UserInfoCursor(section, sortType, null, null, null, null, filterHash);
	}
}
