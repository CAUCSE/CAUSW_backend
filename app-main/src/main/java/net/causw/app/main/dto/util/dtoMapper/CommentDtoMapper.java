package net.causw.app.main.dto.util.dtoMapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.notification.UserCommentSubscribe;
import net.causw.app.main.dto.comment.ChildCommentResponseDto;
import net.causw.app.main.dto.comment.CommentResponseDto;
import net.causw.app.main.dto.comment.CommentSubscribeResponseDto;
import net.causw.app.main.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;

@Mapper(componentModel = "spring")
public interface CommentDtoMapper extends UuidFileToUrlDtoMapper {

	CommentDtoMapper INSTANCE = Mappers.getMapper(CommentDtoMapper.class);

	@Mapping(target = "writerName", source = "comment.writer.name")
	@Mapping(target = "writerNickname", source = "comment.writer.nickname")
	@Mapping(target = "writerAdmissionYear", source = "comment.writer.admissionYear")
	@Mapping(target = "writerProfileImage", source = "comment.writer.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "postId", source = "comment.post.id")
	@Mapping(target = "isAnonymous", source = "comment.isAnonymous")
	@Mapping(target = "numLike", source = "numCommentLike")
	@Mapping(target = "childCommentList", source = "childCommentList")
	@Mapping(target = "isCommentSubscribed", source = "isCommentSubscribed")
	@Mapping(target = "isBlocked", source = "isBlocked")
	@Mapping(target = "content", expression = "java(mapContentForComment(comment.getContent(), isBlocked))")
	CommentResponseDto toCommentResponseDto(
		Comment comment,
		Long numChildComment,
		Long numCommentLike,
		Boolean isCommentLike,
		Boolean isOwner,
		List<ChildCommentResponseDto> childCommentList,
		Boolean updatable,
		Boolean deletable,
		Boolean isCommentSubscribed,
		Boolean isBlocked
	);

	@Mapping(target = "writerName", source = "childComment.writer.name")
	@Mapping(target = "writerNickname", source = "childComment.writer.nickname")
	@Mapping(target = "writerAdmissionYear", source = "childComment.writer.admissionYear")
	@Mapping(target = "writerProfileImage", source = "childComment.writer.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "isAnonymous", source = "childComment.isAnonymous")
	@Mapping(target = "numLike", source = "numChildCommentLike")
	@Mapping(target = "isBlocked", source = "isBlocked")
	@Mapping(target = "content", expression = "java(mapContentForChildComment(childComment.getContent(), isBlocked))")
	ChildCommentResponseDto toChildCommentResponseDto(
		ChildComment childComment,
		Long numChildCommentLike,
		Boolean isChildCommentLike,
		Boolean isOwner,
		Boolean updatable,
		Boolean deletable,
		Boolean isBlocked
	);

	@Mapping(target = "commentId", source = "comment.id")
	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "isSubscribed", source = "isSubscribed")
	CommentSubscribeResponseDto toCommentSubscribeResponseDto(UserCommentSubscribe userCommentSubscribe);

	// Default 메서드들 추가
	default String mapContentForComment(String content, Boolean isBlocked) {
		return Boolean.TRUE.equals(isBlocked) ? null : content;
	}

	default String mapContentForChildComment(String content, Boolean isBlocked) {
		return Boolean.TRUE.equals(isBlocked) ? null : content;
	}
}
