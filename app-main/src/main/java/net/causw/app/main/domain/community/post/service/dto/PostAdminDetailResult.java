package net.causw.app.main.domain.community.post.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.enums.PostAdminStatus;
import net.causw.app.main.domain.community.post.enums.PostCategory;

public record PostAdminDetailResult(
	String postId,
	String title,
	String content,
	PostCategory category,
	String boardId,
	String boardName,
	String writerId,
	String writerName,
	String writerNickname,
	Boolean isAnonymous,
	Boolean isCrawled,
	PostAdminStatus status,
	List<String> imageUrls,
	Long commentCount,
	Long likeCount,
	Long viewCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt) {

	public static PostAdminDetailResult from(Post post, List<String> imageUrls, long commentCount, long likeCount) {
		return new PostAdminDetailResult(
			post.getId(), post.getTitle(), post.getContent(), post.getCategory(),
			post.getBoard().getId(), post.getBoard().getName(), post.getWriter().getId(),
			post.getWriter().getName(), post.getWriter().getNickname(), post.getIsAnonymous(), post.getIsCrawled(),
			PostAdminStatus.from(post), imageUrls, commentCount, likeCount, post.getViewCount(),
			post.getCreatedAt(), post.getUpdatedAt());
	}
}
