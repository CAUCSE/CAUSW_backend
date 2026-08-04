package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.util.List;

public record UserInfoDirectoryResponse(
	UserInfoSummaryResponse myProfile,
	List<UserInfoSectionResponse> sections,
	String nextCursor) {
}
