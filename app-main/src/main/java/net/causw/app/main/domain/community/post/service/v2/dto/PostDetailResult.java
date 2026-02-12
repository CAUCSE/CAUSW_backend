package net.causw.app.main.domain.community.post.service.v2.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record PostDetailResult(
	String id,
	String content,
	Boolean isDeleted,
	String displayWriterNickname,
	String writerProfileImage,
	List<String> fileUrlList,
	Long numComment,
	Long numLike,
	Long numFavorite,
	String voteId,
	Boolean isAnonymous,
	Boolean isOwner,
	Boolean isPostLike,
	Boolean isPostFavorite,
	Boolean updatable,
	Boolean deletable,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	String boardId,
	String boardName) {
}
