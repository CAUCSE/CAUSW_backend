package net.causw.app.main.domain.community.post.repository.query;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.account.enums.user.UserState;

import com.querydsl.core.annotations.QueryProjection;

/**
 * V2 커서 기반 페이징 조회용 DTO (title 없음)
 */
public record PostCursorResult(
	String postId,
	String content,
	long numComment,
	long numLike,
	long numFavorite,
	boolean isAnonymous,
	String voteId,
	boolean isDeleted,
	boolean isCrawled,
	boolean hasWriter,
	String writerName,
	String writerNickname,
	Integer writerAdmissionYear,
	UserState writerUserState,
	String writerProfileImageUrl,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	String boardId,
	String boardName) {
	@QueryProjection
	public PostCursorResult {
	} // canonical constructor
}
