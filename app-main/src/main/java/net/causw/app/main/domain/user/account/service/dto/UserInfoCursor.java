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
	String userInfoId) {

	public static UserInfoCursor sectionStartCursor(UserInfoSectionType section, SortType sortType) {
		return new UserInfoCursor(section, sortType, null, null, null, null);
	}
}
