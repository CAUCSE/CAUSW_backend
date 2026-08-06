package net.causw.app.main.domain.user.account.service.dto.result;

import java.util.List;

public record UserInfoDirectoryResult(
	UserInfoSummaryResult myProfile,
	List<UserInfoSectionResult> sections,
	String nextCursor) {
}
