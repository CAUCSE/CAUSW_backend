package net.causw.app.main.domain.community.comment.service.dto;

public record CommentLikeCountDto(
	String getCommentId,
	Long getLikeCount) {
}
