package net.causw.app.main.domain.community.comment.api.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.community.comment.enums.CommentAdminStatus;

public record CommentAdminResponse(
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
	List<CommentAdminResponse> children) {
}
