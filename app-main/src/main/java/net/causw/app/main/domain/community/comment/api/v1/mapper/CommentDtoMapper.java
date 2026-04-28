package net.causw.app.main.domain.community.comment.api.v1.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.community.comment.api.v1.dto.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.api.v1.dto.CommentResponseDto;
import net.causw.app.main.domain.community.comment.api.v1.dto.CommentSubscribeResponseDto;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.notification.notification.entity.UserCommentSubscribe;
import net.causw.app.main.shared.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;

@Mapper(componentModel = "spring")
public interface CommentDtoMapper extends UuidFileToUrlDtoMapper {

	CommentDtoMapper INSTANCE = Mappers.getMapper(CommentDtoMapper.class);

	@Mapping(target = "id", source = "comment.id")
	@Mapping(target = "createdAt", source = "comment.createdAt")
	@Mapping(target = "updatedAt", source = "comment.updatedAt")
	@Mapping(target = "writerName", source = "comment.writer.name")
	@Mapping(target = "writerNickname", source = "comment.writer.nickname")
	@Mapping(target = "writerAdmissionYear", source = "comment.writer.admissionYear")
	@Mapping(target = "writerProfileImage", expression = "java(writerProfileImage != null && writerProfileImage.getUuidFile() != null ? writerProfileImage.getUuidFile().getFileUrl() : null)")
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
		Boolean isBlocked,
		UserProfileImage writerProfileImage);

	@Mapping(target = "writerName", source = "childComment.writer.name")
	@Mapping(target = "writerNickname", source = "childComment.writer.nickname")
	@Mapping(target = "writerAdmissionYear", source = "childComment.writer.admissionYear")
	@Mapping(target = "id", source = "childComment.id")
	@Mapping(target = "createdAt", source = "childComment.createdAt")
	@Mapping(target = "updatedAt", source = "childComment.updatedAt")
	@Mapping(target = "writerProfileImage", expression = "java(writerProfileImage != null && writerProfileImage.getUuidFile() != null ? writerProfileImage.getUuidFile().getFileUrl() : null)")
	@Mapping(target = "isAnonymous", source = "childComment.isAnonymous")
	@Mapping(target = "numLike", source = "numChildCommentLike")
	@Mapping(target = "isBlocked", source = "isBlocked")
	@Mapping(target = "content", expression = "java(mapContentForChildComment(childComment.getContent(), isBlocked))")
	@Mapping(target = "displayWriterNickname", ignore = true)
	ChildCommentResponseDto toChildCommentResponseDto(
		ChildComment childComment,
		Long numChildCommentLike,
		Boolean isChildCommentLike,
		Boolean isOwner,
		Boolean updatable,
		Boolean deletable,
		Boolean isBlocked,
		UserProfileImage writerProfileImage);

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
