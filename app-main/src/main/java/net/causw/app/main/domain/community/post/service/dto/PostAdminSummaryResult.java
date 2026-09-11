package net.causw.app.main.domain.community.post.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.enums.PostAdminStatus;
import net.causw.app.main.domain.community.post.enums.PostCategory;

public record PostAdminSummaryResult(
	String postId,
	String title,
	String contentPreview,
	PostCategory category,
	String boardId,
	String boardName,
	String writerId,
	String writerName,
	String writerNickname,
	Boolean isAnonymous,
	PostAdminStatus status,
	Long commentCount,
	Long likeCount,
	Long viewCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt) {

	public static PostAdminSummaryResult from(Post post, long commentCount, long likeCount) {
		return new PostAdminSummaryResult(
			post.getId(), post.getTitle(), preview(post.getContent()), post.getCategory(),
			post.getBoard().getId(), post.getBoard().getName(), post.getWriter().getId(),
			post.getWriter().getName(), post.getWriter().getNickname(), post.getIsAnonymous(),
			PostAdminStatus.from(post), commentCount, likeCount, post.getViewCount(), post.getCreatedAt(),
			post.getUpdatedAt());
	}

	private static String preview(String content) {
		if (content == null || content.length() <= 100) {
			return content;
		}
		return content.substring(0, 100);
	}
}
