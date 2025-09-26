package net.causw.app.main.repository.post.query;

import java.time.LocalDateTime;

import net.causw.app.main.domain.model.enums.user.UserState;

import com.querydsl.core.annotations.QueryProjection;

public record PostQueryResult(
	String postId,
	String title,
	String content,
	long numComment,
	long numLike,
	long numFavorite,
	boolean isAnonymous,
	boolean isQuestion,
	boolean isPostVote,
	boolean isPostForm,
	boolean isDeleted,
	boolean hasWriter,
	String writerName,
	String writerNickname,
	Integer writerAdmissionYear,
	UserState writerUserState,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	String postAttachImage
) {
	@QueryProjection
	public PostQueryResult {} // canonical constructor
}
