package net.causw.app.main.domain.community.comment.api.v2.mapper;

import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.service.dto.CommentAuthorInfo;
import net.causw.app.main.domain.community.comment.service.dto.CommentResult;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChildCommentResponseDtoMapper {

	public static ChildCommentResponseDto toResponseDto(CommentResult result) {
		CommentAuthorInfo author = result.authorInfo();
		return ChildCommentResponseDto.builder()
			.id(result.id())
			.content(result.content())
			.isDeleted(result.isDeleted())
			.createdAt(result.createdAt())
			.updatedAt(result.updatedAt())
			.writerName(author.writerName())
			.writerNickname(author.writerNickname())
			.displayWriterNickname(author.displayWriterNickname())
			.writerAdmissionYear(author.writerAdmissionYear())
			.writerProfileImage(author.writerProfileImage())
			.updatable(author.updatable())
			.deletable(author.deletable())
			.isBlocked(author.isBlocked())
			.isAnonymous(author.isAnonymous())
			.isOwner(author.isOwner())
			.isChildCommentLike(result.isCommentLike())
			.numLike(result.numLike())
			.build();
	}

}
