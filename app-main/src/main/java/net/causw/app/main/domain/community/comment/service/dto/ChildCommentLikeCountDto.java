package net.causw.app.main.domain.community.comment.service.dto;

public record ChildCommentLikeCountDto(
	String getChildCommentId,
	Long getLikeCount) {
}
