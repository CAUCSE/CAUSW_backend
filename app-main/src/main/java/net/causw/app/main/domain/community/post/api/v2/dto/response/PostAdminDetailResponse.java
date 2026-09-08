package net.causw.app.main.domain.community.post.api.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.community.post.enums.PostAdminStatus;
import net.causw.app.main.domain.community.post.enums.PostCategory;

public record PostAdminDetailResponse(
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
}
