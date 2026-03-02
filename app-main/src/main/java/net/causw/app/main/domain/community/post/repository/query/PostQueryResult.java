package net.causw.app.main.domain.community.post.repository.query;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.account.enums.user.UserState;

import com.querydsl.core.annotations.QueryProjection;

/**
 * V1 페이지 기반 조회용 DTO (title 포함)
 */
public record PostQueryResult(
	String postId,
	String title,
	String content,
	long numComment,
	long numLike,
	long numFavorite,
	boolean isAnonymous,
	boolean isQuestion,
	boolean isPostVote, // 투표 포함 여부
	boolean isPostForm, // 신청서 포함 여부
	boolean isDeleted,
	boolean hasWriter,
	String writerName,
	String writerNickname,
	Integer writerAdmissionYear,
	UserState writerUserState,
	LocalDateTime writerDeletedAt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	String postAttachImage) { // 썸네일 이미지 URL
	@QueryProjection
	public PostQueryResult {
	} // canonical constructor
}
