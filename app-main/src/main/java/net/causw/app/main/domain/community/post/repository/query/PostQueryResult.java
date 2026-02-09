package net.causw.app.main.domain.community.post.repository.query;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.account.enums.user.UserState;

import com.querydsl.core.annotations.QueryProjection;

public record PostQueryResult(
	String postId,
	String content,
	long numComment,
	long numLike,
	long numFavorite,
	boolean isAnonymous,
	String voteId,
	boolean isDeleted,
	boolean hasWriter,
	String writerName,
	String writerNickname,
	Integer writerAdmissionYear,
	UserState writerUserState,
	String writerProfileImageUrl,
	LocalDateTime createdAt,
	LocalDateTime updatedAt) {
	@QueryProjection
	public PostQueryResult {
	} // canonical constructor
}
