package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.util.List;

public record UserSearchListResponse(
	List<UserSearchItemResponse> users) {

	public record UserSearchItemResponse(
		String id,
		String name,
		String email) {
	}
}
