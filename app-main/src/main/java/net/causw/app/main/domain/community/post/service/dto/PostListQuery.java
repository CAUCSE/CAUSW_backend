package net.causw.app.main.domain.community.post.service.dto;

import java.util.List;

import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.user.account.entity.user.User;

public record PostListQuery(
	User viewer,
	List<String> boardIds,
	String cursor,
	Integer size,
	String keyword,
	PostCategory category) {
	public static PostListQuery of(User viewer, List<String> boardIds, String cursor, Integer size, String keyword,
		PostCategory category) {
		return new PostListQuery(viewer, boardIds, cursor, size, keyword, category);
	}
}
