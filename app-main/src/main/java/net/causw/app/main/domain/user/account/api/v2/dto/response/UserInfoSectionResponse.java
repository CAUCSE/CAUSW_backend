package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.util.List;

import net.causw.app.main.domain.user.account.enums.userinfo.UserInfoSectionType;

public record UserInfoSectionResponse(
	UserInfoSectionType type,
	List<UserInfoSummaryResponse> items,
	boolean hasNext) {
}
