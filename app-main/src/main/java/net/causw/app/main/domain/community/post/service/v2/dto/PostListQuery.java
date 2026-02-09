package net.causw.app.main.domain.community.post.service.v2.dto;

import net.causw.app.main.domain.user.account.entity.user.User;

public record PostListQuery(
	User viewer,
	String boardId,
	String cursor,
	Integer size,
	String keyword) {
	public static PostListQuery of(User viewer, String boardId, String cursor, Integer size, String keyword) {
		return new PostListQuery(viewer, boardId, cursor, size, keyword);
	}
}
