package net.causw.app.main.domain.user.account.service.dto.result;

import java.util.List;

import net.causw.app.main.domain.user.account.enums.userinfo.UserInfoSectionType;

public record UserInfoSectionResult(
	UserInfoSectionType type,
	List<UserInfoSummaryResult> items,
	boolean hasNext) {
}
