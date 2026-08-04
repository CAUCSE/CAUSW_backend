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

	public static UserInfoCursor sectionStartCursor(
		UserInfoSectionType section,
		SortType sortType,
		String filterHash) {
		return new UserInfoCursor(section, sortType, null, null, null, null, filterHash);
	}
}
