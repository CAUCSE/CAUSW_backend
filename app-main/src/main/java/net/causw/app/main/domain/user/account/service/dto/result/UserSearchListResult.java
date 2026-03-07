package net.causw.app.main.domain.user.account.service.dto.result;

import java.util.List;

import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.Builder;

@Builder
public record UserSearchListResult(
	List<UserSearchItemResult> users) {

	@Builder
	public record UserSearchItemResult(
		String id,
		String name,
		String email) {

		public static UserSearchItemResult from(User user) {
			return new UserSearchItemResult(
				user.getId(),
				user.getName(),
				user.getEmail());
		}
	}

	public static UserSearchListResult from(List<User> users) {
		List<UserSearchItemResult> items = users.stream()
			.map(UserSearchItemResult::from)
			.toList();
		return UserSearchListResult.builder()
			.users(items)
			.build();
	}
}
