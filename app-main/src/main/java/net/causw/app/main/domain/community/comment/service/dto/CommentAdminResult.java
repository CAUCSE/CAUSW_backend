package net.causw.app.main.domain.community.comment.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.enums.CommentAdminStatus;

public record CommentAdminResult(
	String commentId,
	String parentCommentId,
	String postId,
	String content,
	CommentAdminStatus status,
	String writerId,
	String writerName,
	String writerNickname,
	Boolean isAnonymous,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	List<CommentAdminResult> children) {

	public static CommentAdminResult from(Comment comment) {
		return new CommentAdminResult(
			comment.getId(),
			comment.getParentComment() == null ? null : comment.getParentComment().getId(),
			comment.getPost().getId(), comment.getContent(), CommentAdminStatus.from(comment),
			comment.getWriter().getId(), comment.getWriter().getName(), comment.getWriter().getNickname(),
			comment.getIsAnonymous(), comment.getCreatedAt(), comment.getUpdatedAt(),
			comment.getChildCommentList().stream().map(CommentAdminResult::from).toList());
	}
}
