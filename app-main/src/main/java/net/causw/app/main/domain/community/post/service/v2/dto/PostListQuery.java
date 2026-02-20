package net.causw.app.main.domain.community.post.service.v2.dto;

import java.util.List;

import net.causw.app.main.domain.user.account.entity.user.User;

public record PostListQuery(
	User viewer,
	List<String> boardIds,
	String cursor,
	Integer size,
	String keyword) {
	public static PostListQuery of(User viewer, List<String> boardIds, String cursor, Integer size, String keyword) {
		return new PostListQuery(viewer, boardIds, cursor, size, keyword);
	}
}
