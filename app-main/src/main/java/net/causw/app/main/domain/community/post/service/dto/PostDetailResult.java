package net.causw.app.main.domain.community.post.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.shared.dto.ProfileImageDto;

import lombok.Builder;

@Builder
public record PostDetailResult(
	String id,
	String title,
	String content,
	Boolean isDeleted,
	String displayWriterNickname,
	ProfileImageDto writerProfileImage,
	List<String> fileUrlList,
	List<CrawledAttachmentResult> crawledAttachments,
	String originalNoticeUrl,
	Long numComment,
	Long numLike,
	Long viewCount,
	String voteId,
	Boolean isAnonymous,
	Boolean isCrawled,
	Boolean isOwner,
	Boolean isPostLike,
	Boolean updatable,
	Boolean deletable,
	Boolean isOfficial,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	String boardId,
	String boardName) {
}
