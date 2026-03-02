package net.causw.app.main.domain.community.comment.api.v2.mapper;

import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentResponseDto;
import net.causw.app.main.domain.community.comment.service.dto.CommentAuthorInfo;
import net.causw.app.main.domain.community.comment.service.dto.CommentResult;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponseDtoMapper {

	public static CommentResponseDto toResponseDto(CommentResult result) {
		CommentAuthorInfo author = result.authorInfo();
		return CommentResponseDto.builder()
			.id(result.id())
			.content(result.content())
			.createdAt(result.createdAt())
			.updatedAt(result.updatedAt())
			.postId(result.postId())
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
			.isCommentLike(result.isCommentLike())
			.isCommentSubscribed(result.isCommentSubscribed())
			.numLike(result.numLike())
			.numChildComment(result.numChildComment())
			.childCommentList(result.childCommentList().stream()
				.map(ChildCommentResponseDtoMapper::toResponseDto)
				.toList())
			.build();
	}

}
