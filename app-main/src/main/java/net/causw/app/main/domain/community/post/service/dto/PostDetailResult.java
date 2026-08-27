package net.causw.app.main.domain.community.post.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.community.post.enums.PostCategory;
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
	Long numComment,
	Long numLike,
	String voteId,
	Boolean isAnonymous,
	Boolean isCrawled,
	PostCategory category,
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
