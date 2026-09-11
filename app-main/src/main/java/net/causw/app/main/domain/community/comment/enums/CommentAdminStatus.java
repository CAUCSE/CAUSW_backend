package net.causw.app.main.domain.community.comment.enums;

import net.causw.app.main.domain.community.comment.entity.Comment;

public enum CommentAdminStatus {
	VISIBLE,
	DELETED;

	public static CommentAdminStatus from(Comment comment) {
		return Boolean.TRUE.equals(comment.getIsDeleted()) ? DELETED : VISIBLE;
	}
}
