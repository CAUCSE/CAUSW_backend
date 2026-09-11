package net.causw.app.main.domain.community.post.enums;

import net.causw.app.main.domain.community.post.entity.Post;

public enum PostAdminStatus {
	VISIBLE,
	HIDDEN,
	DELETED;

	public static PostAdminStatus from(Post post) {
		if (Boolean.TRUE.equals(post.getIsDeleted())) {
			return DELETED;
		}
		return Boolean.TRUE.equals(post.getIsHidden()) ? HIDDEN : VISIBLE;
	}
}
