package net.causw.app.main.domain.community.post.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.community.post.enums.PostAdminStatus;
import net.causw.app.main.domain.community.post.enums.PostCategory;

public record PostAdminSummaryResponse(
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
}
